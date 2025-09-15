package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.AdminStatsDto;
import lk.acpt.smartbiz.dto.BusinessResponse;
import lk.acpt.smartbiz.dto.UsageLogDto;
import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.UsageLog;
import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.*;
import lk.acpt.smartbiz.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        // For AI usage logs, we'll return general usage logs filtered by AI actions
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
            // Log the error but don't fail the main operation
            System.err.println("Failed to log user action: " + e.getMessage());
        }
    }

    private BusinessResponse toBusinessResponse(Business b) {
        return new BusinessResponse(
                b.getBusinessId(),
                b.getOwner().getUserId(),
                b.getOwner().getEmail(),
                b.getBusinessName(),
                b.getAddress(),
                b.getStatus(),
                b.getRegisterDate()
        );
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