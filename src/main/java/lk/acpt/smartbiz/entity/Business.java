package lk.acpt.smartbiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long businessId;

    // owner -> USER.userId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    // UPDATED: Proper relationship with SubscriptionPlan instead of just ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id")
    private SubscriptionPlan subscriptionPlan;

    private String businessName;
    private String address;
    private LocalDateTime registerDate;
    private String status; // e.g., PENDING, ACTIVE

    // DEPRECATED: Keep for backward compatibility but use subscriptionPlan relationship
    @Deprecated
    public Long getSubscriptionPlanId() {
        return subscriptionPlan != null ? subscriptionPlan.getPlanId() : null;
    }

    // DEPRECATED: Keep for backward compatibility
    @Deprecated
    public void setSubscriptionPlanId(Long subscriptionPlanId) {
        // This method is kept for backward compatibility
        // The actual assignment should be done through setSubscriptionPlan()
    }

    // Helper methods
    public boolean hasFeature(String feature) {
        if (subscriptionPlan == null) return false;

        switch (feature.toUpperCase()) {
            case "AI_EMAIL":
                return true; // All plans have basic AI email
            case "AI_INSIGHTS":
            case "AI_MARKETING":
            case "AI_INVOICE_SUMMARY":
            case "ADVANCED_AI":
                return subscriptionPlan.isAiFeatures();
            default:
                return true;
        }
    }

    public boolean canCreateUsers(int currentUserCount) {
        if (subscriptionPlan == null) return currentUserCount < 5; // Default limit
        return currentUserCount < subscriptionPlan.getMaxUsers();
    }

    public boolean canCreateProducts(int currentProductCount) {
        if (subscriptionPlan == null) return currentProductCount < 100; // Default limit
        return currentProductCount < subscriptionPlan.getMaxProducts();
    }

    public boolean canCreateOrders(int currentOrderCount) {
        if (subscriptionPlan == null) return currentOrderCount < 1000; // Default limit
        return currentOrderCount < subscriptionPlan.getMaxOrders();
    }
}