package lk.acpt.smartbiz.repo;

import lk.acpt.smartbiz.entity.Order;
import lk.acpt.smartbiz.entity.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Long> {
    List<OrderDetails> findByOrder(Order order);
}