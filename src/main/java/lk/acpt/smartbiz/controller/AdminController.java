package lk.acpt.smartbiz.controller;

import lk.acpt.smartbiz.dto.AdminStatsDto;
import lk.acpt.smartbiz.dto.BusinessResponse;
import lk.acpt.smartbiz.dto.UsageLogDto;
import lk.acpt.smartbiz.service.AdminService;
import lk.acpt.smartbiz.service.SubscriptionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private SubscriptionPlanService subscriptionPlanService;

    @GetMapping("/stats")
    public ResponseEntity<AdminStatsDto> getSystemStats() {
        AdminStatsDto stats = adminService.getSystemStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/businesses")
    public ResponseEntity<List<BusinessResponse>> getAllBusinesses() {
        List<BusinessResponse> businesses = adminService.getAllBusinesses();
        return ResponseEntity.ok(businesses);
    }

    @GetMapping("/logs/usage")
    public ResponseEntity<List<UsageLogDto>> getUsageLogs(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "50") int size) {
        List<UsageLogDto> logs = adminService.getUsageLogs(page, size);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/logs/ai")
    public ResponseEntity<List<UsageLogDto>> getAIUsageLogs(@RequestParam(defaultValue = "0") int page,
                                                            @RequestParam(defaultValue = "50") int size) {
        List<UsageLogDto> logs = adminService.getAIUsageLogs(page, size);
        return ResponseEntity.ok(logs);
    }

    // NEW: Enhanced system-wide statistics endpoint
    @GetMapping("/system-stats")
    public ResponseEntity<Map<String, Object>> getSystemWideStatistics(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> stats = adminService.getSystemWideStatistics(days);
        return ResponseEntity.ok(stats);
    }

    // NEW: Detailed AI usage statistics
    @GetMapping("/ai-usage-stats")
    public ResponseEntity<Map<String, Object>> getAIUsageStatistics(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> stats = adminService.getAIUsageStatistics(days);
        return ResponseEntity.ok(stats);
    }

    // NEW: Business activity monitoring
    @GetMapping("/business-activity")
    public ResponseEntity<List<Map<String, Object>>> getBusinessActivity(
            @RequestParam(defaultValue = "7") int days) {
        List<Map<String, Object>> activity = adminService.getBusinessActivity(days);
        return ResponseEntity.ok(activity);
    }

    // NEW: User engagement metrics
    @GetMapping("/user-engagement")
    public ResponseEntity<Map<String, Object>> getUserEngagement(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> engagement = adminService.getUserEngagementMetrics(days);
        return ResponseEntity.ok(engagement);
    }

    // ENHANCED: Assign subscription plan to business
    @PostMapping("/business/{businessId}/assign-plan/{planId}")
    public ResponseEntity<Map<String, Object>> assignPlanToBusiness(
            @PathVariable Long businessId,
            @PathVariable Long planId) {
        try {
            boolean success = subscriptionPlanService.assignPlanToBusiness(planId, businessId);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Subscription plan assigned successfully",
                        "businessId", businessId,
                        "planId", planId
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Failed to assign subscription plan",
                        "businessId", businessId,
                        "planId", planId
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error assigning subscription plan: " + e.getMessage(),
                    "businessId", businessId,
                    "planId", planId
            ));
        }
    }

    // NEW: Get subscription analytics
    @GetMapping("/subscription-analytics")
    public ResponseEntity<Map<String, Object>> getSubscriptionAnalytics() {
        Map<String, Object> analytics = adminService.getSubscriptionAnalytics();
        return ResponseEntity.ok(analytics);
    }

    // NEW: Get revenue metrics
    @GetMapping("/revenue-metrics")
    public ResponseEntity<Map<String, Object>> getRevenueMetrics(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> metrics = adminService.getRevenueMetrics(days);
        return ResponseEntity.ok(metrics);
    }

    // NEW: System health check
    @GetMapping("/system-health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = adminService.getSystemHealth();
        return ResponseEntity.ok(health);
    }

    // NEW: Export usage logs
    @GetMapping("/export/usage-logs")
    public ResponseEntity<Map<String, Object>> exportUsageLogs(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "csv") String format) {
        Map<String, Object> export = adminService.exportUsageLogs(days, format);
        return ResponseEntity.ok(export);
    }

    // NEW: Feature usage analytics
    @GetMapping("/feature-usage")
    public ResponseEntity<Map<String, Object>> getFeatureUsage(
            @RequestParam(defaultValue = "30") int days) {
        Map<String, Object> usage = adminService.getFeatureUsage(days);
        return ResponseEntity.ok(usage);
    }
}