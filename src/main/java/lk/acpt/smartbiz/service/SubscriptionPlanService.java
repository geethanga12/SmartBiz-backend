package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.SubscriptionPlanDto;

import java.util.List;

// NEW: Added SubscriptionPlanService interface
public interface SubscriptionPlanService {
    List<SubscriptionPlanDto> getAllPlans();
    List<SubscriptionPlanDto> getAllActivePlans();
    SubscriptionPlanDto getPlanById(Long id);
    SubscriptionPlanDto createPlan(SubscriptionPlanDto planDto);
    SubscriptionPlanDto updatePlan(SubscriptionPlanDto planDto);
    boolean deletePlan(Long id);
    boolean assignPlanToBusiness(Long planId, Long businessId);
}