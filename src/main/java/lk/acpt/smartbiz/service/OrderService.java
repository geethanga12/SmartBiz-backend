package lk.acpt.smartbiz.service;

import lk.acpt.smartbiz.dto.OrderDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OrderService {
    OrderDto saveOrder(OrderDto dto);
    List<OrderDto> getAllOrders();
    OrderDto getOrderById(Long id);
    OrderDto updateOrder(OrderDto dto);
    OrderDto deleteOrder(Long id);
}