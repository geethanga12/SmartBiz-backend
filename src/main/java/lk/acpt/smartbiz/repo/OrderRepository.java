package lk.acpt.smartbiz.repo;

import lk.acpt.smartbiz.entity.Business;
import lk.acpt.smartbiz.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findAllByBusiness(Business business);
}