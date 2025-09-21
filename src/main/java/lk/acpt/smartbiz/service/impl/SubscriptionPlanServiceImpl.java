package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.SubscriptionPlanDto;
import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.SubscriptionPlan;
import lk.acpt.smartbiz.repo.BusinessRepository;
import lk.acpt.smartbiz.repo.SubscriptionPlanRepository;
import lk.acpt.smartbiz.service.SubscriptionPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// NEW: Added SubscriptionPlanServiceImpl
@Service
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepo;

    @Autowired
    private BusinessRepository businessRepo;

    @Override
    public List<SubscriptionPlanDto> getAllPlans() {
        return subscriptionPlanRepo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionPlanDto> getAllActivePlans() {
        return subscriptionPlanRepo.findByIsActiveTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionPlanDto getPlanById(Long id) {
        return subscriptionPlanRepo.findById(id)
                .map(this::toDto)
                .orElse(null);
    }

    @Override
    public SubscriptionPlanDto createPlan(SubscriptionPlanDto planDto) {
        SubscriptionPlan plan = new SubscriptionPlan();
        plan.setPlanName(planDto.getPlanName());
        plan.setMonthlyPrice(planDto.getMonthlyPrice());
        plan.setFeatures(planDto.getFeatures());
        plan.setDuration(planDto.getDuration());
        // use wrapper getter
        plan.setIsActive(planDto.getIsActive());
        plan.setMaxUsers(planDto.getMaxUsers());
        plan.setMaxProducts(planDto.getMaxProducts());
        plan.setMaxOrders(planDto.getMaxOrders());
        // DTO still has primitive isAiFeatures() so this is fine
        plan.setAiFeatures(planDto.isAiFeatures());
        // map description
        plan.setDescription(planDto.getDescription());

        SubscriptionPlan saved = subscriptionPlanRepo.save(plan);
        return toDto(saved);
    }

    @Override
    public SubscriptionPlanDto updatePlan(SubscriptionPlanDto planDto) {
        Optional<SubscriptionPlan> existing = subscriptionPlanRepo.findById(planDto.getPlanId());
        if (existing.isPresent()) {
            SubscriptionPlan plan = existing.get();
            plan.setPlanName(planDto.getPlanName());
            plan.setMonthlyPrice(planDto.getMonthlyPrice());
            plan.setFeatures(planDto.getFeatures());
            plan.setDuration(planDto.getDuration());
            plan.setIsActive(planDto.getIsActive());
            plan.setMaxUsers(planDto.getMaxUsers());
            plan.setMaxProducts(planDto.getMaxProducts());
            plan.setMaxOrders(planDto.getMaxOrders());
            plan.setAiFeatures(planDto.isAiFeatures());
            plan.setDescription(planDto.getDescription());

            SubscriptionPlan updated = subscriptionPlanRepo.save(plan);
            return toDto(updated);
        }
        return null;
    }

    @Override
    public boolean deletePlan(Long id) {
        if (subscriptionPlanRepo.existsById(id)) {
            subscriptionPlanRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean assignPlanToBusiness(Long planId, Long businessId) {
        Optional<SubscriptionPlan> plan = subscriptionPlanRepo.findById(planId);
        Optional<Business> business = businessRepo.findById(businessId);

        if (plan.isPresent() && business.isPresent()) {
            Business b = business.get();
            b.setSubscriptionPlanId(planId);
            businessRepo.save(b);
            return true;
        }
        return false;
    }

    private SubscriptionPlanDto toDto(SubscriptionPlan plan) {
        return new SubscriptionPlanDto(
                plan.getPlanId(),
                plan.getPlanName(),
                plan.getMonthlyPrice(),
                plan.getFeatures(),
                plan.getDuration(),
                // note wrapper boolean mapping
                plan.getIsActive(),
                plan.getMaxUsers(),
                plan.getMaxProducts(),
                plan.getMaxOrders(),
                plan.isAiFeatures(),
                plan.getDescription()
        );
    }
}