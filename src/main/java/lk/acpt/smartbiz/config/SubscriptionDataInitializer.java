package lk.acpt.smartbiz.config;

import lk.acpt.smartbiz.entity.SubscriptionPlan;
import lk.acpt.smartbiz.repo.SubscriptionPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2) // Run after DataInitializer
public class SubscriptionDataInitializer implements CommandLineRunner {

    @Autowired
    private SubscriptionPlanRepository subscriptionPlanRepo;

    @Override
    public void run(String... args) throws Exception {
        initializeDefaultPlans();
    }

    private void initializeDefaultPlans() {
        // Check if plans already exist
        if (subscriptionPlanRepo.count() > 0) {
            System.out.println("Subscription plans already exist, skipping initialization");
            return;
        }

        // Create Basic Plan
        SubscriptionPlan basicPlan = new SubscriptionPlan();
        basicPlan.setPlanName("Basic Plan");
        basicPlan.setMonthlyPrice(29.99);
        basicPlan.setFeatures("Basic business management, Email composer AI, Standard reporting");
        basicPlan.setDuration("Monthly");
        basicPlan.setIsActive(true);
        basicPlan.setMaxUsers(5);
        basicPlan.setMaxProducts(500);
        basicPlan.setMaxOrders(2000);
        basicPlan.setAiFeatures(false); // Only basic AI features
        basicPlan.setDescription("Perfect for small businesses starting their digital transformation. " +
                "Includes essential business management tools and basic AI email composer.");

        // Create Pro Plan
        SubscriptionPlan proPlan = new SubscriptionPlan();
        proPlan.setPlanName("Pro Plan");
        proPlan.setMonthlyPrice(79.99);
        proPlan.setFeatures("Advanced business management, All AI features, Advanced reporting, Priority support");
        proPlan.setDuration("Monthly");
        proPlan.setIsActive(true);
        proPlan.setMaxUsers(25);
        proPlan.setMaxProducts(5000);
        proPlan.setMaxOrders(20000);
        proPlan.setAiFeatures(true); // Full AI features
        proPlan.setDescription("Comprehensive solution for growing businesses. " +
                "Includes all AI features: business insights, marketing post generator, invoice summaries, and advanced analytics.");

        subscriptionPlanRepo.save(basicPlan);
        subscriptionPlanRepo.save(proPlan);

        System.out.println("Default subscription plans created:");
        System.out.println("- Basic Plan: $29.99/month (Limited AI features)");
        System.out.println("- Pro Plan: $79.99/month (All AI features)");
    }
}