package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.OrderDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderService {
    OrderDto saveOrder(OrderDto dto);
    OrderDto getOrderById(Long id);
    List<OrderDto> getAllOrders();
    // More if needed
}