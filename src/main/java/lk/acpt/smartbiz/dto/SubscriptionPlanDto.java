package lk.acpt.smartbiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanDto {
    private Long planId;
    private String planName;
    private double monthlyPrice;
    private String features;
    private String duration;
    // changed to wrapper type
    private Boolean isActive;
    private int maxUsers;
    private int maxProducts;
    private int maxOrders;
    // keep primitive to avoid wider changes, but you could change to Boolean too if you update usages
    private boolean aiFeatures;
    // new
    private String description;
}
