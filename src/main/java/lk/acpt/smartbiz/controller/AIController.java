package lk.acpt.smartbiz.controller;

import lk.acpt.smartbiz.dto.*;
import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.BusinessRepository;
import lk.acpt.smartbiz.repo.UserRepository;
import lk.acpt.smartbiz.service.AIService;
import lk.acpt.smartbiz.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ai")
@PreAuthorize("hasRole('OWNER')")
public class AIController {

    @Autowired
    private AIService aiService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BusinessRepository businessRepo;

    // NEW: Get available AI features for current user's subscription
    @GetMapping("/features")
    public ResponseEntity<Map<String, Object>> getAvailableFeatures(Authentication auth) {
        try {
            User user = userRepo.findByEmail(auth.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Business business = businessRepo.findByOwner(user)
                    .orElseThrow(() -> new RuntimeException("Business not found"));

            Map<String, Object> features = new HashMap<>();
            features.put("AI_EMAIL", true); // All plans have this
            features.put("AI_INSIGHTS", business.hasFeature("AI_INSIGHTS"));
            features.put("AI_MARKETING", business.hasFeature("AI_MARKETING"));
            features.put("AI_INVOICE_SUMMARY", business.hasFeature("AI_INVOICE_SUMMARY"));

            // Plan information
            Map<String, Object> planInfo = new HashMap<>();
            if (business.getSubscriptionPlan() != null) {
                planInfo.put("planName", business.getSubscriptionPlan().getPlanName());
                planInfo.put("hasAiFeatures", business.getSubscriptionPlan().isAiFeatures());
                planInfo.put("monthlyPrice", business.getSubscriptionPlan().getMonthlyPrice());
            } else {
                planInfo.put("planName", "No Plan");
                planInfo.put("hasAiFeatures", false);
                planInfo.put("monthlyPrice", 0.0);
            }

            features.put("planInfo", planInfo);
            features.put("businessId", business.getBusinessId());

            return ResponseEntity.ok(features);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/request")
    public ResponseEntity<AIResponseDto> processAIRequest(@RequestBody AIRequestDto request,
                                                          Authentication auth,
                                                          HttpServletRequest httpRequest) {
        try {
            // Log the AI request
            adminService.logUserAction(
                    auth.getName(),
                    "AI_REQUEST_" + request.getType(),
                    "Prompt: " + request.getPrompt().substring(0, Math.min(request.getPrompt().length(), 100)),
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent")
            );

            AIResponseDto response = aiService.processAIRequest(request, auth.getName());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            AIResponseDto errorResponse = new AIResponseDto(
                    false, null, request.getType(), 0, e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/insights")
    public ResponseEntity<BusinessInsightDto> generateInsights(@RequestBody Map<String, String> request,
                                                               Authentication auth,
                                                               HttpServletRequest httpRequest) {
        try {
            String question = request.get("question");

            adminService.logUserAction(
                    auth.getName(),
                    "AI_INSIGHTS",
                    "Question: " + question,
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent")
            );

            BusinessInsightDto insights = aiService.generateBusinessInsights(question, auth.getName());
            return ResponseEntity.ok(insights);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new BusinessInsightDto(request.get("question"),
                            "Error generating insights: " + e.getMessage(),
                            null, "error", "none"));
        }
    }

    @PostMapping("/email")
    public ResponseEntity<EmailTemplateDto> generateEmail(@RequestBody Map<String, String> request,
                                                          Authentication auth,
                                                          HttpServletRequest httpRequest) {
        try {
            String type = request.get("type");
            String context = request.get("context");

            adminService.logUserAction(
                    auth.getName(),
                    "AI_EMAIL_GENERATION",
                    "Type: " + type + ", Context: " + context,
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent")
            );

            EmailTemplateDto email = aiService.generateEmail(type, context, auth.getName());
            return ResponseEntity.ok(email);
        } catch (Exception e) {
            EmailTemplateDto errorEmail = new EmailTemplateDto("", "Error",
                    "Unable to generate email: " + e.getMessage(), "ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorEmail);
        }
    }

    @PostMapping("/marketing")
    public ResponseEntity<Map<String, String>> generateMarketingPost(@RequestBody Map<String, String> request,
                                                                     Authentication auth,
                                                                     HttpServletRequest httpRequest) {
        try {
            String productInfo = request.get("productInfo");
            String promotion = request.get("promotion");

            adminService.logUserAction(
                    auth.getName(),
                    "AI_MARKETING_POST",
                    "Product: " + productInfo,
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent")
            );

            String post = aiService.generateMarketingPost(productInfo, promotion, auth.getName());
            Map<String, String> response = new HashMap<>();
            response.put("post", post);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/invoice-summary/{orderId}")
    public ResponseEntity<Map<String, String>> generateInvoiceSummary(@PathVariable Long orderId,
                                                                      Authentication auth,
                                                                      HttpServletRequest httpRequest) {
        try {
            adminService.logUserAction(
                    auth.getName(),
                    "AI_INVOICE_SUMMARY",
                    "Order ID: " + orderId,
                    httpRequest.getRemoteAddr(),
                    httpRequest.getHeader("User-Agent")
            );

            String summary = aiService.generateInvoiceSummary(orderId, auth.getName());
            Map<String, String> response = new HashMap<>();
            response.put("summary", summary);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}