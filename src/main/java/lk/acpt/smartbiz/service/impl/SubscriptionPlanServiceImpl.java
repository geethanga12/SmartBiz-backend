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
        plan.setIsActive(planDto.getIsActive() != null ? planDto.getIsActive() : true);
        plan.setMaxUsers(planDto.getMaxUsers() > 0 ? planDto.getMaxUsers() : 10);
        plan.setMaxProducts(planDto.getMaxProducts() > 0 ? planDto.getMaxProducts() : 1000);
        plan.setMaxOrders(planDto.getMaxOrders() > 0 ? planDto.getMaxOrders() : 10000);
        plan.setAiFeatures(planDto.isAiFeatures());
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
            plan.setIsActive(planDto.getIsActive() != null ? planDto.getIsActive() : plan.getIsActive());
            plan.setMaxUsers(planDto.getMaxUsers() > 0 ? planDto.getMaxUsers() : plan.getMaxUsers());
            plan.setMaxProducts(planDto.getMaxProducts() > 0 ? planDto.getMaxProducts() : plan.getMaxProducts());
            plan.setMaxOrders(planDto.getMaxOrders() > 0 ? planDto.getMaxOrders() : plan.getMaxOrders());
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
        try {
            Optional<SubscriptionPlan> planOpt = subscriptionPlanRepo.findById(planId);
            Optional<Business> businessOpt = businessRepo.findById(businessId);

            if (planOpt.isPresent() && businessOpt.isPresent()) {
                Business business = businessOpt.get();
                SubscriptionPlan plan = planOpt.get();

                // FIXED: Proper relationship assignment
                business.setSubscriptionPlan(plan);
                businessRepo.save(business);

                System.out.println("Successfully assigned plan " + plan.getPlanName() +
                        " to business " + business.getBusinessName());
                return true;
            } else {
                System.err.println("Plan or business not found. PlanId: " + planId +
                        ", BusinessId: " + businessId);
                return false;
            }
        } catch (Exception e) {
            System.err.println("Error assigning plan to business: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private SubscriptionPlanDto toDto(SubscriptionPlan plan) {
        return new SubscriptionPlanDto(
                plan.getPlanId(),
                plan.getPlanName(),
                plan.getMonthlyPrice(),
                plan.getFeatures(),
                plan.getDuration(),
                plan.getIsActive(),
                plan.getMaxUsers(),
                plan.getMaxProducts(),
                plan.getMaxOrders(),
                plan.isAiFeatures(),
                plan.getDescription()
        );
    }
}