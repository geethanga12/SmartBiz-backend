package lk.acpt.smartbiz.repo;

import lk.acpt.smartbiz.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
}