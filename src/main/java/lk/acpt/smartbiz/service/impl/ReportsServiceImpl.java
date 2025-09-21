package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.ReportRequestDto;
import lk.acpt.smartbiz.entity.*;
import lk.acpt.smartbiz.repo.*;
import lk.acpt.smartbiz.service.ReportsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.text.DecimalFormat;

// IMPORTS needed for PDFBox:
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import java.io.ByteArrayOutputStream;

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
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            // Format currencies and numbers
            DecimalFormat currencyFormat = new DecimalFormat("#,##0.00");
            DecimalFormat percentFormat = new DecimalFormat("#,##0.00");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float y = yStart;
            float leading = 16f;
            float sectionSpacing = 25f;

            // Helper method to check if we need a new page
            y = checkAndAddNewPage(doc, cs, y, margin, 100);

            // Document Header
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 20);
            cs.newLineAtOffset(margin, y);
            cs.showText("SmartBiz Business Report");
            cs.endText();
            y -= leading * 1.5f;

            // Report Title
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
            cs.newLineAtOffset(margin, y);
            String title = getReportTitle(reportType);
            cs.showText(title);
            cs.endText();
            y -= leading;

            // Generated timestamp
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
            cs.newLineAtOffset(margin, y);
            cs.showText("Generated: " + LocalDateTime.now().format(dateFormatter));
            cs.endText();
            y -= sectionSpacing;

            // Report period (if available)
            if (reportData.containsKey("period")) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA, 12);
                cs.newLineAtOffset(margin, y);
                cs.showText("Period: " + reportData.get("period"));
                cs.endText();
                y -= sectionSpacing;
            }

            // Draw a separator line
            cs.setLineWidth(1f);
            cs.moveTo(margin, y);
            cs.lineTo(page.getMediaBox().getWidth() - margin, y);
            cs.stroke();
            y -= 20;

            // Generate content based on report type
            switch (reportType.toUpperCase()) {
                case "SALES":
                    y = generateSalesPDFContent(cs, page, reportData, y, margin, leading, currencyFormat);
                    break;
                case "INVENTORY":
                    y = generateInventoryPDFContent(cs, page, reportData, y, margin, leading, currencyFormat);
                    break;
                case "PROFIT_LOSS":
                    y = generateProfitLossPDFContent(cs, page, reportData, y, margin, leading, currencyFormat, percentFormat);
                    break;
                case "CUSTOMER_ANALYSIS":
                    y = generateCustomerAnalysisPDFContent(cs, page, reportData, y, margin, leading, currencyFormat);
                    break;
                default:
                    y = generateGenericPDFContent(cs, page, reportData, y, margin, leading);
                    break;
            }

            cs.close();
            doc.save(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            return generateFallbackPDF(e.getMessage());
        }
    }

    private String getReportTitle(String reportType) {
        switch (reportType.toUpperCase()) {
            case "SALES": return "Sales Performance Report";
            case "INVENTORY": return "Inventory Analysis Report";
            case "PROFIT_LOSS": return "Profit & Loss Statement";
            case "CUSTOMER_ANALYSIS": return "Customer Analysis Report";
            default: return reportType.toUpperCase() + " Report";
        }
    }

    private float checkAndAddNewPage(PDDocument doc, PDPageContentStream cs, float y, float margin, float requiredSpace) throws Exception {
        if (y < margin + requiredSpace) {
            cs.close();
            PDPage newPage = new PDPage(PDRectangle.LETTER);
            doc.addPage(newPage);
            cs = new PDPageContentStream(doc, newPage);
            return newPage.getMediaBox().getHeight() - margin;
        }
        return y;
    }

    private float generateSalesPDFContent(PDPageContentStream cs, PDPage page, Map<String, Object> reportData,
                                          float y, float margin, float leading, DecimalFormat currencyFormat) throws Exception {

        // Summary Section
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(margin, y);
        cs.showText("Sales Summary");
        cs.endText();
        y -= leading * 1.5f;

        cs.setFont(PDType1Font.HELVETICA, 12);

        // Sales metrics
        if (reportData.containsKey("totalSales")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Total Sales: LKR " + currencyFormat.format((Double) reportData.get("totalSales")));
            cs.endText();
            y -= leading;
        }

        if (reportData.containsKey("totalOrders")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Total Orders: " + reportData.get("totalOrders"));
            cs.endText();
            y -= leading;
        }

        if (reportData.containsKey("averageOrderValue")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Average Order Value: LKR " + currencyFormat.format((Double) reportData.get("averageOrderValue")));
            cs.endText();
            y -= leading * 2;
        }

        // Top Selling Items Section
        if (reportData.containsKey("topSellingItems")) {
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
            cs.newLineAtOffset(margin, y);
            cs.showText("Top Selling Items");
            cs.endText();
            y -= leading * 1.5f;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> topItems = (List<Map<String, Object>>) reportData.get("topSellingItems");

            cs.setFont(PDType1Font.HELVETICA, 11);
            int rank = 1;
            for (Map<String, Object> item : topItems) {
                if (y < 100) break; // Avoid page overflow for now

                cs.beginText();
                cs.newLineAtOffset(margin + 20, y);
                cs.showText(rank + ". " + item.get("name") + " - Quantity Sold: " + item.get("quantitySold"));
                cs.endText();
                y -= leading;
                rank++;
            }
        }

        return y;
    }

    private float generateInventoryPDFContent(PDPageContentStream cs, PDPage page, Map<String, Object> reportData,
                                              float y, float margin, float leading, DecimalFormat currencyFormat) throws Exception {

        // Inventory Summary
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(margin, y);
        cs.showText("Inventory Overview");
        cs.endText();
        y -= leading * 1.5f;

        cs.setFont(PDType1Font.HELVETICA, 12);

        if (reportData.containsKey("totalItems")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Total Items: " + reportData.get("totalItems"));
            cs.endText();
            y -= leading;
        }

        if (reportData.containsKey("totalInventoryValue")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Total Inventory Value: LKR " + currencyFormat.format((Double) reportData.get("totalInventoryValue")));
            cs.endText();
            y -= leading;
        }

        if (reportData.containsKey("lowStockCount")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Low Stock Items: " + reportData.get("lowStockCount"));
            cs.endText();
            y -= leading * 2;
        }

        // Low Stock Items Details
        if (reportData.containsKey("lowStockItems")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> lowStockItems = (List<Map<String, Object>>) reportData.get("lowStockItems");

            if (!lowStockItems.isEmpty()) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.newLineAtOffset(margin, y);
                cs.showText("Low Stock Alert");
                cs.endText();
                y -= leading * 1.5f;

                cs.setFont(PDType1Font.HELVETICA, 11);
                for (Map<String, Object> item : lowStockItems) {
                    if (y < 100) break;

                    cs.beginText();
                    cs.newLineAtOffset(margin + 20, y);
                    String itemInfo = String.format("• %s - Qty: %s, Price: LKR %s, Value: LKR %s",
                            item.get("name"),
                            item.get("quantity"),
                            currencyFormat.format((Double) item.get("unitPrice")),
                            currencyFormat.format((Double) item.get("value")));
                    cs.showText(itemInfo);
                    cs.endText();
                    y -= leading;
                }
            }
        }

        return y;
    }

    private float generateProfitLossPDFContent(PDPageContentStream cs, PDPage page, Map<String, Object> reportData,
                                               float y, float margin, float leading, DecimalFormat currencyFormat,
                                               DecimalFormat percentFormat) throws Exception {

        // Revenue Section
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(margin, y);
        cs.showText("Revenue & Costs");
        cs.endText();
        y -= leading * 1.5f;

        cs.setFont(PDType1Font.HELVETICA, 12);

        if (reportData.containsKey("totalRevenue")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Total Revenue: LKR " + currencyFormat.format((Double) reportData.get("totalRevenue")));
            cs.endText();
            y -= leading;
        }

        if (reportData.containsKey("totalCOGS")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Cost of Goods Sold: LKR " + currencyFormat.format((Double) reportData.get("totalCOGS")));
            cs.endText();
            y -= leading;
        }

        if (reportData.containsKey("grossProfit")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Gross Profit: LKR " + currencyFormat.format((Double) reportData.get("grossProfit")));
            cs.endText();
            y -= leading;
        }

        if (reportData.containsKey("totalExpenses")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Total Expenses: LKR " + currencyFormat.format((Double) reportData.get("totalExpenses")));
            cs.endText();
            y -= leading * 2;
        }

        // Profit Summary
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(margin, y);
        cs.showText("Profitability");
        cs.endText();
        y -= leading * 1.5f;

        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);

        if (reportData.containsKey("netProfit")) {
            Double netProfit = (Double) reportData.get("netProfit");
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Net Profit: LKR " + currencyFormat.format(netProfit) + (netProfit >= 0 ? " (PROFIT)" : " (LOSS)"));
            cs.endText();
            y -= leading;
        }

        if (reportData.containsKey("netMargin")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Net Margin: " + percentFormat.format((Double) reportData.get("netMargin")) + "%");
            cs.endText();
            y -= leading;
        }

        if (reportData.containsKey("grossMargin")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Gross Margin: " + percentFormat.format((Double) reportData.get("grossMargin")) + "%");
            cs.endText();
            y -= leading * 2;
        }

        // Expense Breakdown
        if (reportData.containsKey("expenseByCategory")) {
            @SuppressWarnings("unchecked")
            Map<String, Double> expenseByCategory = (Map<String, Double>) reportData.get("expenseByCategory");

            if (!expenseByCategory.isEmpty()) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.newLineAtOffset(margin, y);
                cs.showText("Expense Breakdown");
                cs.endText();
                y -= leading * 1.5f;

                cs.setFont(PDType1Font.HELVETICA, 11);
                for (Map.Entry<String, Double> entry : expenseByCategory.entrySet()) {
                    if (y < 100) break;

                    cs.beginText();
                    cs.newLineAtOffset(margin + 20, y);
                    cs.showText("• " + entry.getKey() + ": LKR " + currencyFormat.format(entry.getValue()));
                    cs.endText();
                    y -= leading;
                }
            }
        }

        return y;
    }

    private float generateCustomerAnalysisPDFContent(PDPageContentStream cs, PDPage page, Map<String, Object> reportData,
                                                     float y, float margin, float leading, DecimalFormat currencyFormat) throws Exception {

        // Customer Overview
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
        cs.newLineAtOffset(margin, y);
        cs.showText("Customer Overview");
        cs.endText();
        y -= leading * 1.5f;

        cs.setFont(PDType1Font.HELVETICA, 12);

        if (reportData.containsKey("totalCustomers")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Total Customers: " + reportData.get("totalCustomers"));
            cs.endText();
            y -= leading;
        }

        if (reportData.containsKey("activeCustomers")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Active Customers: " + reportData.get("activeCustomers"));
            cs.endText();
            y -= leading;
        }

        if (reportData.containsKey("averageCustomerSpending")) {
            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText("Average Customer Spending: LKR " + currencyFormat.format((Double) reportData.get("averageCustomerSpending")));
            cs.endText();
            y -= leading * 2;
        }

        // Top Customers
        if (reportData.containsKey("topCustomers")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> topCustomers = (List<Map<String, Object>>) reportData.get("topCustomers");

            if (!topCustomers.isEmpty()) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 14);
                cs.newLineAtOffset(margin, y);
                cs.showText("Top Customers");
                cs.endText();
                y -= leading * 1.5f;

                cs.setFont(PDType1Font.HELVETICA, 11);
                int rank = 1;
                for (Map<String, Object> customer : topCustomers) {
                    if (y < 100) break;

                    cs.beginText();
                    cs.newLineAtOffset(margin + 20, y);
                    String customerInfo = String.format("%d. %s (%s) - Spent: LKR %s, Orders: %s",
                            rank,
                            customer.get("name"),
                            customer.get("email"),
                            currencyFormat.format((Double) customer.get("totalSpent")),
                            customer.get("orderCount"));
                    cs.showText(customerInfo);
                    cs.endText();
                    y -= leading;
                    rank++;
                }
            }
        }

        return y;
    }

    private float generateGenericPDFContent(PDPageContentStream cs, PDPage page, Map<String, Object> reportData,
                                            float y, float margin, float leading) throws Exception {

        cs.setFont(PDType1Font.HELVETICA, 12);

        for (Map.Entry<String, Object> entry : reportData.entrySet()) {
            if (y < 100) break; // Avoid page overflow

            String key = entry.getKey();
            Object value = entry.getValue();

            // Skip complex objects for generic display
            if (value instanceof Map || value instanceof List) {
                continue;
            }

            cs.beginText();
            cs.newLineAtOffset(margin + 20, y);
            cs.showText(key + ": " + String.valueOf(value));
            cs.endText();
            y -= leading;
        }

        return y;
    }

    private byte[] generateFallbackPDF(String errorMessage) {
        try (PDDocument fallback = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            fallback.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(fallback, page);

            // Header
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
            cs.newLineAtOffset(50, 700);
            cs.showText("SmartBiz Report - Error");
            cs.endText();

            // Error message
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 12);
            cs.newLineAtOffset(50, 650);
            cs.showText("Failed to generate detailed report.");
            cs.endText();

            cs.beginText();
            cs.newLineAtOffset(50, 630);
            cs.showText("Error: " + (errorMessage.length() > 100 ? errorMessage.substring(0, 100) + "..." : errorMessage));
            cs.endText();

            // Generated timestamp
            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
            cs.newLineAtOffset(50, 600);
            cs.showText("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            cs.endText();

            cs.close();
            fallback.save(baos);
            return baos.toByteArray();

        } catch (Exception ex) {
            throw new RuntimeException("PDF generation completely failed: " + ex.getMessage(), ex);
        }
    }
}