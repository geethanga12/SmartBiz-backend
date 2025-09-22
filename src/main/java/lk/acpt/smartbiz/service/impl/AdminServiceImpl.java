package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.AdminStatsDto;
import lk.acpt.smartbiz.dto.BusinessResponse;
import lk.acpt.smartbiz.dto.UsageLogDto;
import lk.acpt.smartbiz.entity.*;
import lk.acpt.smartbiz.repo.*;
import lk.acpt.smartbiz.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private BusinessRepository businessRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private AIUsageRepository aiUsageRepo;

    @Autowired
    private UsageLogRepository usageLogRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ExpenseRepository expenseRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private ItemRepository itemRepo;

    @Autowired
    private EmployeeRepository employeeRepo;

    @Override
    public AdminStatsDto getSystemStats() {
        AdminStatsDto stats = new AdminStatsDto();

        // Basic counts
        stats.setTotalBusinesses(businessRepo.count());
        stats.setTotalUsers(userRepo.count());

        LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
        stats.setTotalAIRequests(aiUsageRepo.countSuccessfulRequestsSince(lastMonth));

        Double totalCost = aiUsageRepo.getTotalCostSince(lastMonth);
        stats.setTotalAICosts(totalCost != null ? totalCost : 0.0);

        // Businesses by status
        Map<String, Long> businessesByStatus = new HashMap<>();
        List<Business> businesses = businessRepo.findAll();
        businessesByStatus.put("ACTIVE", businesses.stream().filter(b -> "ACTIVE".equals(b.getStatus())).count());
        businessesByStatus.put("PENDING", businesses.stream().filter(b -> "PENDING".equals(b.getStatus())).count());
        stats.setBusinessesByStatus(businessesByStatus);

        // AI requests by type
        Map<String, Long> aiRequestsByType = new HashMap<>();
        List<Object[]> requestTypes = aiUsageRepo.getRequestTypeStatsSince(lastMonth);
        for (Object[] row : requestTypes) {
            aiRequestsByType.put((String) row[0], (Long) row[1]);
        }
        stats.setAiRequestsByType(aiRequestsByType);

        // Recent logs
        Page<UsageLog> recentLogs = usageLogRepo.findAllByOrderByTimestampDesc(PageRequest.of(0, 10));
        stats.setRecentLogs(recentLogs.getContent().stream().map(this::toUsageLogDto).collect(Collectors.toList()));

        // System health
        Map<String, Object> systemHealth = new HashMap<>();
        systemHealth.put("status", "healthy");
        systemHealth.put("uptime", "24/7");
        systemHealth.put("lastUpdated", LocalDateTime.now());
        stats.setSystemHealth(systemHealth);

        return stats;
    }

    @Override
    public List<BusinessResponse> getAllBusinesses() {
        return businessRepo.findAll().stream()
                .map(this::toBusinessResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsageLogDto> getUsageLogs(int page, int size) {
        Page<UsageLog> logs = usageLogRepo.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
        return logs.getContent().stream()
                .map(this::toUsageLogDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UsageLogDto> getAIUsageLogs(int page, int size) {
        Page<UsageLog> logs = usageLogRepo.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
        return logs.getContent().stream()
                .filter(log -> log.getAction().contains("AI") || log.getAction().contains("GENERATE"))
                .map(this::toUsageLogDto)
                .collect(Collectors.toList());
    }

    @Override
    public void logUserAction(String userEmail, String action, String details, String ipAddress, String userAgent) {
        try {
            User user = userRepo.findByEmail(userEmail).orElse(null);
            if (user == null) return;

            Business business = businessRepo.findByOwner(user).orElse(null);

            UsageLog log = new UsageLog();
            log.setUser(user);
            log.setBusiness(business);
            log.setAction(action);
            log.setDetails(details);
            log.setTimestamp(LocalDateTime.now());
            log.setIpAddress(ipAddress);
            log.setUserAgent(userAgent);

            usageLogRepo.save(log);
        } catch (Exception e) {
            System.err.println("Failed to log user action: " + e.getMessage());
        }
    }

    // NEW: Enhanced analytics and monitoring methods
    @Override
    public Map<String, Object> getSystemWideStatistics(int days) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        try {
            // Business statistics
            long totalBusinesses = businessRepo.count();
            long activeBusinesses = businessRepo.findAll().stream()
                    .filter(b -> "ACTIVE".equals(b.getStatus())).count();

            // User statistics
            long totalUsers = userRepo.count();

            // Order statistics
            List<Order> allOrders = orderRepo.findAll();
            double totalRevenue = allOrders.stream().mapToDouble(Order::getAmount).sum();
            long recentOrders = allOrders.stream()
                    .filter(o -> o.getDate().isAfter(since)).count();

            // AI usage statistics
            long totalAIRequests = aiUsageRepo.countSuccessfulRequestsSince(since);
            Double totalAICosts = aiUsageRepo.getTotalCostSince(since);

            stats.put("totalBusinesses", totalBusinesses);
            stats.put("activeBusinesses", activeBusinesses);
            stats.put("totalUsers", totalUsers);
            stats.put("totalRevenue", totalRevenue);
            stats.put("recentOrders", recentOrders);
            stats.put("totalAIRequests", totalAIRequests);
            stats.put("totalAICosts", totalAICosts != null ? totalAICosts : 0.0);
            stats.put("period", days + " days");
            stats.put("generatedAt", LocalDateTime.now());

        } catch (Exception e) {
            stats.put("error", "Unable to fetch complete statistics: " + e.getMessage());
        }

        return stats;
    }

    @Override
    public Map<String, Object> getAIUsageStatistics(int days) {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        try {
            // AI usage by type
            List<Object[]> requestTypes = aiUsageRepo.getRequestTypeStatsSince(since);
            Map<String, Long> usageByType = new HashMap<>();
            for (Object[] row : requestTypes) {
                usageByType.put((String) row[0], (Long) row[1]);
            }

            // Top AI-using businesses
            List<Business> businesses = businessRepo.findAll();
            Map<String, Long> businessUsage = new HashMap<>();
            for (Business business : businesses) {
                long count = aiUsageRepo.countRequestsBetweenDates(business, since, LocalDateTime.now());
                if (count > 0) {
                    businessUsage.put(business.getBusinessName(), count);
                }
            }

            // Sort top businesses by usage
            List<Map<String, Object>> topBusinesses = businessUsage.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .map(entry -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("businessName", entry.getKey());
                        item.put("requests", entry.getValue());
                        return item;
                    })
                    .collect(Collectors.toList());

            stats.put("usageByType", usageByType);
            stats.put("topBusinesses", topBusinesses);
            stats.put("totalRequests", usageByType.values().stream().mapToLong(Long::longValue).sum());
            stats.put("period", days + " days");
            stats.put("generatedAt", LocalDateTime.now());

        } catch (Exception e) {
            stats.put("error", "Unable to fetch AI usage statistics: " + e.getMessage());
        }

        return stats;
    }

    @Override
    public List<Map<String, Object>> getBusinessActivity(int days) {
        List<Map<String, Object>> activity = new ArrayList<>();
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        try {
            List<Business> businesses = businessRepo.findAll();
            for (Business business : businesses) {
                Map<String, Object> businessActivity = new HashMap<>();
                businessActivity.put("businessId", business.getBusinessId());
                businessActivity.put("businessName", business.getBusinessName());
                businessActivity.put("status", business.getStatus());

                // Count recent orders
                long orders = orderRepo.findAllByBusiness(business).stream()
                        .filter(o -> o.getDate().isAfter(since)).count();

                // Count AI requests
                long aiRequests = aiUsageRepo.countRequestsBetweenDates(business, since, LocalDateTime.now());

                // Count usage logs
                long actions = usageLogRepo.findAllByBusinessOrderByTimestampDesc(business).stream()
                        .filter(log -> log.getTimestamp().isAfter(since)).count();

                businessActivity.put("recentOrders", orders);
                businessActivity.put("aiRequests", aiRequests);
                businessActivity.put("totalActions", actions);
                businessActivity.put("subscriptionPlan",
                        business.getSubscriptionPlan() != null ?
                                business.getSubscriptionPlan().getPlanName() : "No Plan");

                activity.add(businessActivity);
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Unable to fetch business activity: " + e.getMessage());
            activity.add(error);
        }

        return activity;
    }

    @Override
    public Map<String, Object> getUserEngagementMetrics(int days) {
        Map<String, Object> metrics = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        try {
            // Active users (users with recent actions)
            long activeUsers = usageLogRepo.findLogsBetweenDates(since, LocalDateTime.now())
                    .stream()
                    .map(log -> log.getUser().getEmail())
                    .collect(Collectors.toSet())
                    .size();

            // Most active users
            Map<String, Long> userActions = new HashMap<>();
            usageLogRepo.findLogsBetweenDates(since, LocalDateTime.now())
                    .forEach(log -> {
                        String email = log.getUser().getEmail();
                        userActions.merge(email, 1L, Long::sum);
                    });

            List<Map<String, Object>> topUsers = userActions.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(10)
                    .map(entry -> {
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", entry.getKey());
                        user.put("actions", entry.getValue());
                        return user;
                    })
                    .collect(Collectors.toList());

            // Action types frequency
            List<Object[]> actionStats = usageLogRepo.getActionStatsSince(since);
            Map<String, Long> actionTypes = new HashMap<>();
            for (Object[] row : actionStats) {
                actionTypes.put((String) row[0], (Long) row[1]);
            }

            metrics.put("activeUsers", activeUsers);
            metrics.put("topUsers", topUsers);
            metrics.put("actionTypes", actionTypes);
            metrics.put("totalUsers", userRepo.count());
            metrics.put("period", days + " days");
            metrics.put("generatedAt", LocalDateTime.now());

        } catch (Exception e) {
            metrics.put("error", "Unable to fetch user engagement metrics: " + e.getMessage());
        }

        return metrics;
    }

    @Override
    public Map<String, Object> getSubscriptionAnalytics() {
        Map<String, Object> analytics = new HashMap<>();

        try {
            List<Business> businesses = businessRepo.findAll();

            // Subscription distribution
            Map<String, Long> planDistribution = new HashMap<>();
            Map<String, Double> revenueByPlan = new HashMap<>();

            for (Business business : businesses) {
                String planName = business.getSubscriptionPlan() != null ?
                        business.getSubscriptionPlan().getPlanName() : "No Plan";

                planDistribution.merge(planName, 1L, Long::sum);

                if (business.getSubscriptionPlan() != null) {
                    double price = business.getSubscriptionPlan().getMonthlyPrice();
                    revenueByPlan.merge(planName, price, Double::sum);
                }
            }

            // Calculate total potential monthly revenue
            double totalMonthlyRevenue = revenueByPlan.values().stream()
                    .mapToDouble(Double::doubleValue).sum();

            analytics.put("planDistribution", planDistribution);
            analytics.put("revenueByPlan", revenueByPlan);
            analytics.put("totalMonthlyRevenue", totalMonthlyRevenue);
            analytics.put("totalBusinesses", businesses.size());
            analytics.put("businessesWithPlans",
                    businesses.stream().filter(b -> b.getSubscriptionPlan() != null).count());
            analytics.put("generatedAt", LocalDateTime.now());

        } catch (Exception e) {
            analytics.put("error", "Unable to fetch subscription analytics: " + e.getMessage());
        }

        return analytics;
    }

    @Override
    public Map<String, Object> getRevenueMetrics(int days) {
        Map<String, Object> metrics = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        try {
            // Total system revenue from orders
            List<Order> allOrders = orderRepo.findAll();
            double totalRevenue = allOrders.stream()
                    .filter(o -> o.getDate().isAfter(since))
                    .mapToDouble(Order::getAmount).sum();

            // Revenue by business
            Map<String, Double> revenueByBusiness = new HashMap<>();
            for (Order order : allOrders) {
                if (order.getDate().isAfter(since)) {
                    String businessName = order.getBusiness().getBusinessName();
                    revenueByBusiness.merge(businessName, order.getAmount(), Double::sum);
                }
            }

            // Top earning businesses
            List<Map<String, Object>> topBusinesses = revenueByBusiness.entrySet().stream()
                    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                    .limit(10)
                    .map(entry -> {
                        Map<String, Object> business = new HashMap<>();
                        business.put("businessName", entry.getKey());
                        business.put("revenue", entry.getValue());
                        return business;
                    })
                    .collect(Collectors.toList());

            // Subscription revenue (potential monthly)
            double subscriptionRevenue = businessRepo.findAll().stream()
                    .filter(b -> b.getSubscriptionPlan() != null)
                    .mapToDouble(b -> b.getSubscriptionPlan().getMonthlyPrice())
                    .sum();

            metrics.put("totalRevenue", totalRevenue);
            metrics.put("topBusinesses", topBusinesses);
            metrics.put("monthlySubscriptionRevenue", subscriptionRevenue);
            metrics.put("averageOrderValue",
                    allOrders.isEmpty() ? 0 : totalRevenue / allOrders.size());
            metrics.put("period", days + " days");
            metrics.put("generatedAt", LocalDateTime.now());

        } catch (Exception e) {
            metrics.put("error", "Unable to fetch revenue metrics: " + e.getMessage());
        }

        return metrics;
    }

    @Override
    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Database connectivity
            long businessCount = businessRepo.count();
            long userCount = userRepo.count();

            // Recent activity
            LocalDateTime lastHour = LocalDateTime.now().minusHours(1);
            long recentLogs = usageLogRepo.countLogsSince(lastHour);

            // System status
            health.put("status", "HEALTHY");
            health.put("databaseConnected", true);
            health.put("totalBusinesses", businessCount);
            health.put("totalUsers", userCount);
            health.put("recentActivity", recentLogs);
            health.put("uptime", "Available");
            health.put("lastCheck", LocalDateTime.now());

            // Memory usage (basic)
            Runtime runtime = Runtime.getRuntime();
            health.put("memoryUsed", runtime.totalMemory() - runtime.freeMemory());
            health.put("memoryTotal", runtime.totalMemory());
            health.put("memoryFree", runtime.freeMemory());

        } catch (Exception e) {
            health.put("status", "ERROR");
            health.put("error", e.getMessage());
            health.put("lastCheck", LocalDateTime.now());
        }

        return health;
    }

    @Override
    public Map<String, Object> exportUsageLogs(int days, String format) {
        Map<String, Object> export = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        try {
            List<UsageLog> logs = usageLogRepo.findLogsBetweenDates(since, LocalDateTime.now());

            if ("csv".equalsIgnoreCase(format)) {
                StringBuilder csv = new StringBuilder();
                csv.append("Timestamp,User,Business,Action,Details,IP Address\n");

                for (UsageLog log : logs) {
                    csv.append(String.format("%s,%s,%s,%s,%s,%s\n",
                            log.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            log.getUser().getEmail(),
                            log.getBusiness() != null ? log.getBusiness().getBusinessName() : "System",
                            log.getAction(),
                            log.getDetails() != null ? log.getDetails().replaceAll(",", ";") : "",
                            log.getIpAddress() != null ? log.getIpAddress() : ""
                    ));
                }

                export.put("format", "csv");
                export.put("data", csv.toString());
                export.put("filename", "usage_logs_" + days + "_days.csv");
            } else {
                // JSON format
                List<Map<String, Object>> jsonLogs = logs.stream()
                        .map(this::logToMap)
                        .collect(Collectors.toList());

                export.put("format", "json");
                export.put("data", jsonLogs);
                export.put("filename", "usage_logs_" + days + "_days.json");
            }

            export.put("totalRecords", logs.size());
            export.put("period", days + " days");
            export.put("generatedAt", LocalDateTime.now());

        } catch (Exception e) {
            export.put("error", "Unable to export usage logs: " + e.getMessage());
        }

        return export;
    }

    @Override
    public Map<String, Object> getFeatureUsage(int days) {
        Map<String, Object> usage = new HashMap<>();
        LocalDateTime since = LocalDateTime.now().minusDays(days);

        try {
            // Feature usage from action logs
            List<Object[]> actionStats = usageLogRepo.getActionStatsSince(since);
            Map<String, Long> featureUsage = new HashMap<>();

            for (Object[] row : actionStats) {
                String action = (String) row[0];
                Long count = (Long) row[1];

                // Categorize actions into features
                String feature = categorizeAction(action);
                featureUsage.merge(feature, count, Long::sum);
            }

            // AI feature usage
            List<Object[]> aiStats = aiUsageRepo.getRequestTypeStatsSince(since);
            Map<String, Long> aiFeatureUsage = new HashMap<>();
            for (Object[] row : aiStats) {
                aiFeatureUsage.put((String) row[0], (Long) row[1]);
            }

            usage.put("generalFeatures", featureUsage);
            usage.put("aiFeatures", aiFeatureUsage);
            usage.put("period", days + " days");
            usage.put("generatedAt", LocalDateTime.now());

        } catch (Exception e) {
            usage.put("error", "Unable to fetch feature usage: " + e.getMessage());
        }

        return usage;
    }

    // Helper methods
    private String categorizeAction(String action) {
        if (action.contains("ORDER")) return "Order Management";
        if (action.contains("CUSTOMER")) return "Customer Management";
        if (action.contains("ITEM") || action.contains("INVENTORY")) return "Inventory Management";
        if (action.contains("EMPLOYEE")) return "Employee Management";
        if (action.contains("SUPPLIER")) return "Supplier Management";
        if (action.contains("REPORT")) return "Reports";
        if (action.contains("AI")) return "AI Features";
        if (action.contains("LOGIN") || action.contains("LOGOUT")) return "Authentication";
        return "Other";
    }

    private Map<String, Object> logToMap(UsageLog log) {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", log.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        map.put("user", log.getUser().getEmail());
        map.put("business", log.getBusiness() != null ? log.getBusiness().getBusinessName() : "System");
        map.put("action", log.getAction());
        map.put("details", log.getDetails());
        map.put("ipAddress", log.getIpAddress());
        return map;
    }

    private BusinessResponse toBusinessResponse(Business b) {
        BusinessResponse response = new BusinessResponse();
        response.setBusinessId(b.getBusinessId());
        response.setOwnerId(b.getOwner().getUserId());
        response.setOwnerEmail(b.getOwner().getEmail());
        response.setBusinessName(b.getBusinessName());
        response.setAddress(b.getAddress());
        response.setStatus(b.getStatus());
        response.setRegisterDate(b.getRegisterDate());

        // Include subscription plan details
        if (b.getSubscriptionPlan() != null) {
            SubscriptionPlan plan = b.getSubscriptionPlan();
            response.setSubscriptionPlanId(plan.getPlanId());
            response.setSubscriptionPlanName(plan.getPlanName());
            response.setSubscriptionPlanPrice(plan.getMonthlyPrice());
            response.setHasAiFeatures(plan.isAiFeatures());
            response.setMaxUsers(plan.getMaxUsers());
            response.setMaxProducts(plan.getMaxProducts());
            response.setMaxOrders(plan.getMaxOrders());
        } else {
            response.setSubscriptionPlanId(null);
            response.setSubscriptionPlanName("No Plan");
            response.setSubscriptionPlanPrice(0.0);
            response.setHasAiFeatures(false);
            response.setMaxUsers(5);
            response.setMaxProducts(100);
            response.setMaxOrders(1000);
        }

        return response;
    }

    private UsageLogDto toUsageLogDto(UsageLog log) {
        return new UsageLogDto(
                log.getLogId(),
                log.getBusiness() != null ? log.getBusiness().getBusinessId() : null,
                log.getBusiness() != null ? log.getBusiness().getBusinessName() : "System",
                log.getUser().getUserId(),
                log.getUser().getEmail(),
                log.getAction(),
                log.getDetails(),
                log.getTimestamp(),
                log.getIpAddress()
        );
    }
}