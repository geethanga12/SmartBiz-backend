package lk.acpt.smartbiz.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lk.acpt.smartbiz.dto.*;
import lk.acpt.smartbiz.entity.*;
import lk.acpt.smartbiz.repo.*;
import lk.acpt.smartbiz.service.AIService;
import lk.acpt.smartbiz.util.GeminiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AIServiceImpl implements AIService {

    @Autowired
    private GeminiClient geminiClient;

    @Autowired
    private AIUsageRepository aiUsageRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BusinessRepository businessRepo;

    @Autowired
    private OrderRepository orderRepo;

    @Autowired
    private CustomerRepository customerRepo;

    @Autowired
    private ItemRepository itemRepo;

    @Autowired
    private ExpenseRepository expenseRepo;

    @Autowired
    private ObjectMapper objectMapper;

    private Business getCurrentBusiness(String userEmail) {
        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return businessRepo.findByOwner(user)
                .orElseThrow(() -> new RuntimeException("Business not found"));
    }

    private User getCurrentUser(String userEmail) {
        return userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private void logAIUsage(String requestType, String prompt, String response,
                            int tokensUsed, boolean successful, String errorMessage,
                            Business business, User user) {
        AIUsage usage = new AIUsage();
        usage.setBusiness(business);
        usage.setUser(user);
        usage.setRequestType(requestType);
        usage.setPrompt(prompt);
        usage.setResponse(response);
        usage.setRequestTime(LocalDateTime.now());
        usage.setTokensUsed(tokensUsed);
        usage.setCost(tokensUsed * 0.001); // Rough cost calculation
        usage.setSuccessful(successful);
        usage.setErrorMessage(errorMessage);
        aiUsageRepo.save(usage);
    }

    @Override
    public AIResponseDto processAIRequest(AIRequestDto request, String userEmail) {
        try {
            Business business = getCurrentBusiness(userEmail);
            User user = getCurrentUser(userEmail);

            String response = null;
            switch (request.getType().toUpperCase()) {
                case "BUSINESS_INSIGHTS":
                    BusinessInsightDto insight = generateBusinessInsights(request.getPrompt(), userEmail);
                    response = insight.getAnswer();
                    break;
                case "EMAIL_GENERATOR":
                    EmailTemplateDto email = generateEmail("GENERAL", request.getPrompt(), userEmail);
                    response = "Subject: " + email.getSubject() + "\n\n" + email.getBody();
                    break;
                case "MARKETING_POST":
                    response = generateMarketingPost(request.getPrompt(), "promotion", userEmail);
                    break;
                case "INVOICE_SUMMARY":
                    response = generateInvoiceSummary(Long.parseLong(request.getPrompt()), userEmail);
                    break;
                default:
                    throw new RuntimeException("Unknown AI request type: " + request.getType());
            }

            int tokensUsed = estimateTokens(request.getPrompt() + response);
            logAIUsage(request.getType(), request.getPrompt(), response, tokensUsed, true, null, business, user);

            return new AIResponseDto(true, response, request.getType(), tokensUsed, null);

        } catch (Exception e) {
            Business business = getCurrentBusiness(userEmail);
            User user = getCurrentUser(userEmail);
            logAIUsage(request.getType(), request.getPrompt(), null, 0, false, e.getMessage(), business, user);
            return new AIResponseDto(false, null, request.getType(), 0, e.getMessage());
        }
    }

    // UPDATED: Improved business insights with concise, actionable responses
    @Override
    public BusinessInsightDto generateBusinessInsights(String question, String userEmail) {
        try {
            Business business = getCurrentBusiness(userEmail);
            String businessContext = buildBusinessContext(business);

            // UPDATED: More focused prompt for concise responses
            String prompt = String.format(
                    "You are a business analyst for %s. Answer this question concisely in 2-3 paragraphs: %s\n\n" +
                            "Business Data:\n%s\n\n" +
                            "Provide a direct, actionable answer. Focus on key insights and specific recommendations. " +
                            "Keep response under 300 words. Use bullet points for recommendations.",
                    business.getBusinessName(), question, businessContext
            );

            String answer = geminiClient.generateContent(prompt);

            // UPDATED: Clean up the response format
            answer = formatBusinessInsightResponse(answer);

            Map<String, Object> data = buildBusinessDataMap(business);

            return new BusinessInsightDto(question, answer, data, "current", "table");

        } catch (Exception e) {
            throw new RuntimeException("Error generating business insights: " + e.getMessage(), e);
        }
    }

    // UPDATED: Improved email generation with better formatting
    @Override
    public EmailTemplateDto generateEmail(String type, String context, String userEmail) {
        try {
            Business business = getCurrentBusiness(userEmail);

            String prompt = String.format(
                    "Generate a professional email for %s business. " +
                            "Type: %s. Context: %s. " +
                            "Keep it concise (under 150 words), professional, and friendly. " +
                            "Format: Subject: [subject]\n\n[email body]",
                    business.getBusinessName(), type, context
            );

            String content = geminiClient.generateContent(prompt);

            // Parse subject and body from response
            String[] parts = content.split("\n\n", 2);
            String subject = parts[0].replace("Subject:", "").replace("Subject", "").trim();
            String body = parts.length > 1 ? parts[1] : content;

            return new EmailTemplateDto("", subject, body, type);

        } catch (Exception e) {
            throw new RuntimeException("Error generating email: " + e.getMessage(), e);
        }
    }

    // UPDATED: Improved marketing post generation
    @Override
    public String generateMarketingPost(String productInfo, String promotion, String userEmail) {
        try {
            Business business = getCurrentBusiness(userEmail);

            String prompt = String.format(
                    "Create a catchy social media post for %s. " +
                            "Product/Service: %s. Promotion: %s. " +
                            "Keep it under 150 characters, engaging, with relevant hashtags and call-to-action. " +
                            "Make it social media ready.",
                    business.getBusinessName(), productInfo, promotion
            );

            String content = geminiClient.generateContent(prompt);

            // UPDATED: Clean up the response to remove any extra formatting
            return content.trim();

        } catch (Exception e) {
            throw new RuntimeException("Error generating marketing post: " + e.getMessage(), e);
        }
    }

    // UPDATED: Improved invoice summary generation
    @Override
    public String generateInvoiceSummary(Long orderId, String userEmail) {
        try {
            Business business = getCurrentBusiness(userEmail);
            Order order = orderRepo.findById(orderId)
                    .filter(o -> o.getBusiness().equals(business))
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            StringBuilder orderInfo = new StringBuilder();
            orderInfo.append(String.format("Order #%d for %s\n", order.getOrderId(), order.getCustomer().getCustomerName()));
            orderInfo.append(String.format("Date: %s\n", order.getDate().toLocalDate()));
            orderInfo.append(String.format("Total: $%.2f\n", order.getAmount()));
            orderInfo.append("Items:\n");

            for (OrderDetail detail : order.getDetails()) {
                orderInfo.append(String.format("- %s x%d @ $%.2f each\n",
                        detail.getItem().getName(),
                        detail.getOrderItemQuantity(),
                        detail.getPrice()));
            }

            String prompt = String.format(
                    "Explain this invoice in 2-3 simple sentences for the customer: %s " +
                            "Make it friendly, clear, and include key details. Keep under 100 words.",
                    orderInfo.toString()
            );

            String summary = geminiClient.generateContent(prompt);
            return summary.trim();

        } catch (Exception e) {
            throw new RuntimeException("Error generating invoice summary: " + e.getMessage(), e);
        }
    }

    // UPDATED: More concise business context building
    private String buildBusinessContext(Business business) {
        StringBuilder context = new StringBuilder();

        try {
            // Sales data
            List<Order> orders = orderRepo.findAllByBusiness(business);
            double totalSales = orders.stream().mapToDouble(Order::getAmount).sum();
            context.append(String.format("Total Sales: $%.2f\n", totalSales));

            // Customer data
            List<Customer> customers = customerRepo.findAllByBusiness(business);
            context.append(String.format("Total Customers: %d\n", customers.size()));

            // Inventory data
            List<Item> items = itemRepo.findAllByBusiness(business);
            double inventoryValue = items.stream().mapToDouble(item -> item.getQuantity() * item.getUnitPrice()).sum();
            context.append(String.format("Inventory Value: $%.2f\n", inventoryValue));
            context.append(String.format("Product Count: %d\n", items.size()));

            // Recent orders (last 30 days)
            LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
            long recentOrders = orders.stream()
                    .filter(o -> o.getDate().isAfter(thirtyDaysAgo))
                    .count();
            context.append(String.format("Orders Last 30 Days: %d\n", recentOrders));

            // Top selling items (limit to top 3)
            Map<String, Integer> itemSales = new HashMap<>();
            orders.forEach(order -> {
                order.getDetails().forEach(detail -> {
                    String itemName = detail.getItem().getName();
                    itemSales.merge(itemName, detail.getOrderItemQuantity(), Integer::sum);
                });
            });

            List<String> topItems = itemSales.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            if (!topItems.isEmpty()) {
                context.append("Top Products: ").append(String.join(", ", topItems));
            }

        } catch (Exception e) {
            context.append("Business data temporarily unavailable.");
        }

        return context.toString();
    }

    // UPDATED: Build comprehensive business data map
    private Map<String, Object> buildBusinessDataMap(Business business) {
        Map<String, Object> data = new HashMap<>();

        try {
            List<Order> orders = orderRepo.findAllByBusiness(business);
            List<Customer> customers = customerRepo.findAllByBusiness(business);
            List<Item> items = itemRepo.findAllByBusiness(business);
            List<Expense> expenses = expenseRepo.findAllByBusiness(business);

            data.put("totalSales", orders.stream().mapToDouble(Order::getAmount).sum());
            data.put("totalOrders", orders.size());
            data.put("totalCustomers", customers.size());
            data.put("totalProducts", items.size());
            data.put("totalExpenses", expenses.stream().mapToDouble(Expense::getAmount).sum());
            data.put("inventoryValue", items.stream().mapToDouble(item ->
                    item.getQuantity() * item.getUnitPrice()).sum());

            // UPDATED: Add recent activity data
            LocalDateTime lastMonth = LocalDateTime.now().minusMonths(1);
            long recentOrders = orders.stream()
                    .filter(o -> o.getDate().isAfter(lastMonth))
                    .count();
            data.put("recentOrders", recentOrders);

        } catch (Exception e) {
            data.put("error", "Unable to fetch complete business data");
        }

        return data;
    }

    // NEW: Helper method to format business insight responses
    private String formatBusinessInsightResponse(String response) {
        // Remove excessive asterisks, formatting, and long explanations
        String cleaned = response.replaceAll("\\*\\*([^*]+)\\*\\*", "$1"); // Remove bold formatting
        cleaned = cleaned.replaceAll("\\*([^*]+)\\*", "$1"); // Remove italic formatting
        cleaned = cleaned.replaceAll("---+", ""); // Remove separator lines

        // Split into sentences and limit length
        String[] sentences = cleaned.split("\\. ");
        StringBuilder formatted = new StringBuilder();
        int sentenceCount = 0;

        for (String sentence : sentences) {
            if (sentenceCount >= 8) break; // Limit to 8 sentences max
            if (sentence.trim().length() > 10) { // Skip very short fragments
                formatted.append(sentence.trim());
                if (!sentence.endsWith(".")) formatted.append(".");
                formatted.append(" ");
                sentenceCount++;
            }
        }

        return formatted.toString().trim();
    }

    private int estimateTokens(String text) {
        // Rough estimation: 1 token ≈ 4 characters
        return text.length() / 4;
    }
}