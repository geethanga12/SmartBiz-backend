package lk.acpt.smartbiz.repo;

import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByBusiness(Business business);

    // For dashboard
    @Query("SELECT SUM(o.amount) FROM Order o WHERE o.business = ?1 AND o.date >= ?2 AND o.date < ?3")
    Double getSalesBetweenDates(Business business, LocalDateTime start, LocalDateTime end);
}