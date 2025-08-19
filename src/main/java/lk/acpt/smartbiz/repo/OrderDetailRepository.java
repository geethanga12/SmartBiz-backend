package lk.acpt.smartbiz.repo;

import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    @Query("SELECT SUM(d.orderItemQuantity * d.item.costPrice) FROM OrderDetail d WHERE d.order.business = ?1 AND d.order.date >= ?2 AND d.order.date < ?3")
    Double getCostsBetweenDates(Business b, LocalDateTime start, LocalDateTime end);
}