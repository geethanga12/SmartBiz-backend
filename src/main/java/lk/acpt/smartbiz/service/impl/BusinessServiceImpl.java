package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.BusinessRequest;
import lk.acpt.smartbiz.dto.BusinessResponse;
import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.SubscriptionPlan;
import lk.acpt.smartbiz.entity.User;
import lk.acpt.smartbiz.repo.BusinessRepository;
import lk.acpt.smartbiz.repo.SubscriptionPlanRepository;
import lk.acpt.smartbiz.repo.UserRepository;
import lk.acpt.smartbiz.service.BusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BusinessServiceImpl implements BusinessService {

    @Autowired
    private BusinessRepository businessRepo;

    @Autowired
    private UserRepository userRepo;

    // ADDED: Subscription plan repository for proper relationship handling
    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepo;

    @Override
    public BusinessResponse createBusiness(BusinessRequest req, String ownerEmail) {
        User owner = userRepo.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));

        // if owner already has business - you can return or throw
        if (businessRepo.findByOwner(owner).isPresent()) {
            throw new RuntimeException("Owner already has a business");
        }

        Business b = new Business();
        b.setOwner(owner);
        b.setBusinessName(req.getBusinessName());
        b.setAddress(req.getAddress());
        b.setRegisterDate(LocalDateTime.now());
        b.setStatus("ACTIVE");

        // UPDATED: Assign default subscription plan (first active plan or null)
        subscriptionPlanRepo.findByIsActiveTrue().stream()
                .findFirst()
                .ifPresent(b::setSubscriptionPlan);

        Business saved = businessRepo.save(b);

        return toDto(saved);
    }

    @Override
    public BusinessResponse getBusinessForOwner(String ownerEmail) {
        User owner = userRepo.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Owner not found"));
        Business b = businessRepo.findByOwner(owner)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        return toDto(b);
    }

    @Override
    public List<BusinessResponse> listAllBusinesses() {
        return businessRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    // UPDATED: Enhanced toDto method with subscription plan information
    private BusinessResponse toDto(Business b) {
        BusinessResponse response = new BusinessResponse();
        response.setBusinessId(b.getBusinessId());
        response.setOwnerId(b.getOwner().getUserId());
        response.setOwnerEmail(b.getOwner().getEmail());
        response.setBusinessName(b.getBusinessName());
        response.setAddress(b.getAddress());
        response.setStatus(b.getStatus());
        response.setRegisterDate(b.getRegisterDate());

        // UPDATED: Include subscription plan details
        if (b.getSubscriptionPlan() != null) {
            SubscriptionPlan plan = b.getSubscriptionPlan();
            response.setSubscriptionPlanId(plan.getPlanId());
            response.setSubscriptionPlanName(plan.getPlanName());
            response.setSubscriptionPlanPrice(plan.getMonthlyPrice());
            response.setHasAiFeatures(plan.isAiFeatures());
            response.setMaxUsers(plan.getMaxUsers());
            response.setMaxProducts(plan.getMaxProducts());
            response.setMaxOrders(plan.getMaxOrders());
        } else {
            response.setSubscriptionPlanId(null);
            response.setSubscriptionPlanName("No Plan");
            response.setSubscriptionPlanPrice(0.0);
            response.setHasAiFeatures(false);
            response.setMaxUsers(5);
            response.setMaxProducts(100);
            response.setMaxOrders(1000);
        }

        return response;
    }
}