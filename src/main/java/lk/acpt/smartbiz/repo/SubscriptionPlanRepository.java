package lk.acpt.smartbiz.repo;

import lk.acpt.smartbiz.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// NEW: Added SubscriptionPlanRepository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findByIsActiveTrue();
    List<SubscriptionPlan> findByPlanNameContainingIgnoreCase(String planName);
    List<SubscriptionPlan> findByMonthlyPriceLessThanEqual(double maxPrice);
}