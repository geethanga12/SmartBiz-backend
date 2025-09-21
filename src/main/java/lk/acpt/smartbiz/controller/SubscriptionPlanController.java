package lk.acpt.smartbiz.controller;

import lk.acpt.smartbiz.dto.SubscriptionPlanDto;
import lk.acpt.smartbiz.service.SubscriptionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// NEW: Added subscription plan management controller
@RestController
@RequestMapping("/api/v1/subscription-plans")
@PreAuthorize("hasRole('ADMIN')")
public class SubscriptionPlanController {

    @Autowired
    private SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    public ResponseEntity<List<SubscriptionPlanDto>> getAllPlans() {
        return ResponseEntity.ok(subscriptionPlanService.getAllPlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getPlan(@PathVariable Long id) {
        SubscriptionPlanDto plan = subscriptionPlanService.getPlanById(id);
        if (plan != null) {
            return ResponseEntity.ok(plan);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Subscription plan not found"));
    }

    @PostMapping
    public ResponseEntity<SubscriptionPlanDto> createPlan(@RequestBody SubscriptionPlanDto planDto) {
        SubscriptionPlanDto created = subscriptionPlanService.createPlan(planDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updatePlan(@PathVariable Long id, @RequestBody SubscriptionPlanDto planDto) {
        planDto.setPlanId(id);
        SubscriptionPlanDto updated = subscriptionPlanService.updatePlan(planDto);
        if (updated != null) {
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Subscription plan not found"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deletePlan(@PathVariable Long id) {
        boolean deleted = subscriptionPlanService.deletePlan(id);
        if (deleted) {
            return ResponseEntity.ok(Map.of("message", "Subscription plan deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Subscription plan not found"));
    }

    // NEW: Public endpoint for businesses to view available plans
    @GetMapping("/public")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<SubscriptionPlanDto>> getPublicPlans() {
        return ResponseEntity.ok(subscriptionPlanService.getAllActivePlans());
    }

    // NEW: Assign plan to business
    @PostMapping("/{planId}/assign/{businessId}")
    public ResponseEntity<Object> assignPlanToBusiness(@PathVariable Long planId, @PathVariable Long businessId) {
        boolean assigned = subscriptionPlanService.assignPlanToBusiness(planId, businessId);
        if (assigned) {
            return ResponseEntity.ok(Map.of("message", "Plan assigned to business successfully"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Failed to assign plan to business"));
    }
}