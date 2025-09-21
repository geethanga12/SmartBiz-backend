package lk.acpt.smartbiz.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long planId;

    private String planName;

    private double monthlyPrice;

    private String features;

    private String duration;

    // UPDATED: Added new fields for better plan management
    @Column(columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(columnDefinition = "int default 10")
    private Integer maxUsers = 10;

    @Column(columnDefinition = "int default 1000")
    private Integer maxProducts = 1000;

    @Column(columnDefinition = "int default 10000")
    private Integer maxOrders = 10000;

    @Column(columnDefinition = "boolean default false")
    private boolean aiFeatures = false;

    // UPDATED: Added description field
    @Column(columnDefinition = "TEXT")
    private String description;

    // Helper methods for backward compatibility
    public Boolean getIsActive() {
        return isActive != null ? isActive : true;
    }

    public Integer getMaxUsers() {
        return maxUsers != null ? maxUsers : 10;
    }

    public Integer getMaxProducts() {
        return maxProducts != null ? maxProducts : 1000;
    }

    public Integer getMaxOrders() {
        return maxOrders != null ? maxOrders : 10000;
    }
}