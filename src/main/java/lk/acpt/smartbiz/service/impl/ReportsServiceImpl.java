package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.ReportRequestDto;
import lk.acpt.smartbiz.entity.*;
import lk.acpt.smartbiz.repo.*;
import lk.acpt.smartbiz.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportsServiceImpl implements ReportsService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BusinessRepository businessRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private ItemRepository itemRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private ExpenseRepository expenseRepo;

    private Business getCurrentBusiness(String userEmail) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return businessRepo.findByOwner(user)
                .orElseThrow(() -> new RuntimeException("Business not found"));
    }

    @Override
    public Map<String, Object> generateSalesReport(ReportRequestDto request, String userEmail) {
        Business business = getCurrentBusiness(userEmail);
        Map<String, Object> report = new HashMap<>();

        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

        List<Order> orders = orderRepo.findAllByBusiness(business).stream()
                .filter(order -> order.getDate().isAfter(startDateTime) && order.getDate().isBefore(endDateTime))
                .collect(Collectors.toList());

        double totalSales = orders.stream().mapToDouble(Order::getAmount).sum();
        int totalOrders = orders.size();
        double averageOrderValue = totalOrders > 0 ? totalSales / totalOrders : 0;

        // Daily sales breakdown
        Map<LocalDate, Double> dailySales = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getDate().toLocalDate(),
                        Collectors.summingDouble(Order::getAmount)
                ));

        // Top selling items
        Map<String, Integer> itemSales = new HashMap<>();
        orders.forEach(order -> {
            order.getDetails().forEach(detail -> {
                String itemName = detail.getItem().getName();
                itemSales.merge(itemName, detail.getOrderItemQuantity(), Integer::sum);
            });
        });

        List<Map<String, Object>> topItems = itemSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("quantitySold", entry.getValue());
                    return item;
                })
                .collect(Collectors.toList());

        report.put("reportType", "SALES");
        report.put("period", request.getStartDate() + " to " + request.getEndDate());
        report.put("totalSales", totalSales);
        report.put("totalOrders", totalOrders);
        report.put("averageOrderValue", averageOrderValue);
        report.put("dailySales", dailySales);
        report.put("topSellingItems", topItems);
        report.put("generatedAt", LocalDateTime.now());

        return report;
    }

    @Override
    public Map<String, Object> generateInventoryReport(ReportRequestDto request, String userEmail) {
        Business business = getCurrentBusiness(userEmail);
        Map<String, Object> report = new HashMap<>();

        List<Item> items = itemRepo.findAllByBusiness(business);

        double totalInventoryValue = items.stream()
                .mapToDouble(item -> item.getQuantity() * item.getUnitPrice())
                .sum();

        // Low stock items (less than 10 quantity)
        List<Map<String, Object>> lowStockItems = items.stream()
                .filter(item -> item.getQuantity() < 10)
                .map(item -> {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("name", item.getName());
                    itemData.put("quantity", item.getQuantity());
                    itemData.put("unitPrice", item.getUnitPrice());
                    itemData.put("value", item.getQuantity() * item.getUnitPrice());
                    return itemData;
                })
                .collect(Collectors.toList());

        // All items summary
        List<Map<String, Object>> allItems = items.stream()
                .map(item -> {
                    Map<String, Object> itemData = new HashMap<>();
                    itemData.put("name", item.getName());
                    itemData.put("quantity", item.getQuantity());
                    itemData.put("unitPrice", item.getUnitPrice());
                    itemData.put("costPrice", item.getCostPrice());
                    itemData.put("value", item.getQuantity() * item.getUnitPrice());
                    itemData.put("supplier", item.getSupplier() != null ? item.getSupplier().getName() : "N/A");
                    return itemData;
                })
                .collect(Collectors.toList());

        report.put("reportType", "INVENTORY");
        report.put("totalItems", items.size());
        report.put("totalInventoryValue", totalInventoryValue);
        report.put("lowStockItems", lowStockItems);
        report.put("lowStockCount", lowStockItems.size());
        if (request.isIncludeDetails()) {
            report.put("allItems", allItems);
        }
        report.put("generatedAt", LocalDateTime.now());

        return report;
    }

    @Override
    public Map<String, Object> generateProfitLossReport(ReportRequestDto request, String userEmail) {
        Business business = getCurrentBusiness(userEmail);
        Map<String, Object> report = new HashMap<>();

        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

        // Sales (Revenue)
        List<Order> orders = orderRepo.findAllByBusiness(business).stream()
                .filter(order -> order.getDate().isAfter(startDateTime) && order.getDate().isBefore(endDateTime))
                .collect(Collectors.toList());

        double totalRevenue = orders.stream().mapToDouble(Order::getAmount).sum();

        // Calculate COGS (Cost of Goods Sold)
        double totalCOGS = 0;
        for (Order order : orders) {
            for (OrderDetail detail : order.getDetails()) {
                totalCOGS += detail.getOrderItemQuantity() * detail.getItem().getCostPrice();
            }
        }

        // Expenses
        List<Expense> expenses = expenseRepo.findAllByBusiness(business).stream()
                .filter(expense -> expense.getDate().isAfter(startDateTime) && expense.getDate().isBefore(endDateTime))
                .collect(Collectors.toList());

        double totalExpenses = expenses.stream().mapToDouble(Expense::getAmount).sum();

        // Calculate profit/loss
        double grossProfit = totalRevenue - totalCOGS;
        double netProfit = grossProfit - totalExpenses;
        double grossMargin = totalRevenue > 0 ? (grossProfit / totalRevenue) * 100 : 0;
        double netMargin = totalRevenue > 0 ? (netProfit / totalRevenue) * 100 : 0;

        // Expense breakdown by category
        Map<String, Double> expenseByCategory = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getCategory() != null ? expense.getCategory() : "Other",
                        Collectors.summingDouble(Expense::getAmount)
                ));

        report.put("reportType", "PROFIT_LOSS");
        report.put("period", request.getStartDate() + " to " + request.getEndDate());
        report.put("totalRevenue", totalRevenue);
        report.put("totalCOGS", totalCOGS);
        report.put("grossProfit", grossProfit);
        report.put("totalExpenses", totalExpenses);
        report.put("netProfit", netProfit);
        report.put("grossMargin", grossMargin);
        report.put("netMargin", netMargin);
        report.put("expenseByCategory", expenseByCategory);
        report.put("generatedAt", LocalDateTime.now());

        return report;
    }

    @Override
    public Map<String, Object> generateCustomerAnalysisReport(ReportRequestDto request, String userEmail) {
        Business business = getCurrentBusiness(userEmail);
        Map<String, Object> report = new HashMap<>();

        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);

        List<Customer> customers = customerRepo.findAllByBusiness(business);
        List<Order> orders = orderRepo.findAllByBusiness(business).stream()
                .filter(order -> order.getDate().isAfter(startDateTime) && order.getDate().isBefore(endDateTime))
                .collect(Collectors.toList());

        // Customer spending analysis
        Map<Long, Double> customerSpending = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCustomer().getCustomerId(),
                        Collectors.summingDouble(Order::getAmount)
                ));

        // Top customers
        List<Map<String, Object>> topCustomers = customerSpending.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Customer customer = customers.stream()
                            .filter(c -> c.getCustomerId().equals(entry.getKey()))
                            .findFirst().orElse(null);

                    Map<String, Object> customerData = new HashMap<>();
                    customerData.put("name", customer != null ? customer.getCustomerName() : "Unknown");
                    customerData.put("email", customer != null ? customer.getEmail() : "Unknown");
                    customerData.put("totalSpent", entry.getValue());
                    customerData.put("orderCount", orders.stream()
                            .filter(o -> o.getCustomer().getCustomerId().equals(entry.getKey()))
                            .count());
                    return customerData;
                })
                .collect(Collectors.toList());

        double totalCustomerSpending = customerSpending.values().stream().mapToDouble(Double::doubleValue).sum();
        double averageCustomerSpending = customerSpending.size() > 0 ? totalCustomerSpending / customerSpending.size() : 0;

        report.put("reportType", "CUSTOMER_ANALYSIS");
        report.put("period", request.getStartDate() + " to " + request.getEndDate());
        report.put("totalCustomers", customers.size());
        report.put("activeCustomers", customerSpending.size());
        report.put("totalCustomerSpending", totalCustomerSpending);
        report.put("averageCustomerSpending", averageCustomerSpending);
        report.put("topCustomers", topCustomers);
        report.put("generatedAt", LocalDateTime.now());

        return report;
    }

    @Override
    public byte[] generatePDFReport(String reportType, Map<String, Object> reportData) {
        // PDF generation would typically use iText or similar library
        // For now, returning a simple placeholder
        String pdfContent = "PDF Report: " + reportType + "\n" + reportData.toString();
        return pdfContent.getBytes();
    }
}