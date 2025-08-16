package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.OrderDetailsDto;
import lk.acpt.smartbiz.dto.OrderDto;
import lk.acpt.smartbiz.entity.*;
import lk.acpt.smartbiz.repo.*;
import lk.acpt.smartbiz.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final OrderDetailsRepository orderDetailsRepo;
    private final CustomerRepository customerRepo;
    private final EmployeeRepository employeeRepo;
    private final ItemRepository itemRepo;
    private final UserRepository userRepo;
    private final BusinessRepository businessRepo;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepo, OrderDetailsRepository orderDetailsRepo, CustomerRepository customerRepo, EmployeeRepository employeeRepo, ItemRepository itemRepo, UserRepository userRepo, BusinessRepository businessRepo) {
        this.orderRepo = orderRepo;
        this.orderDetailsRepo = orderDetailsRepo;
        this.customerRepo = customerRepo;
        this.employeeRepo = employeeRepo;
        this.itemRepo = itemRepo;
        this.userRepo = userRepo;
        this.businessRepo = businessRepo;
    }

    private Business currentBusiness() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User owner = userRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Owner not found"));
        return businessRepo.findByOwner(owner).orElseThrow(() -> new RuntimeException("Business not found"));
    }

    private OrderDto toDto(Order o) {
        List<OrderDetailsDto> detailsDtos = new ArrayList<>();
        List<OrderDetails> details = orderDetailsRepo.findByOrder(o);
        for (OrderDetails d : details) {
            OrderDetailsDto dto = new OrderDetailsDto(
                    d.getItem().getItemId(),
                    d.getQuantity(),
                    d.getPrice(),
                    d.getDiscount()
            );
            detailsDtos.add(dto);
        }

        return new OrderDto(
                o.getOrderId(),
                o.getCustomer().getCustomerId(),
                o.getEmployee() != null ? o.getEmployee().getEmployeeId() : null,
                o.getDate(),
                o.getAmount(),
                detailsDtos
        );
    }

    @Override
    @Transactional
    public OrderDto saveOrder(OrderDto dto) {
        Business business = currentBusiness();
        Customer customer = customerRepo.findByIdAndBusiness(dto.getCustomerId(), business)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Employee employee = dto.getEmployeeId() != null ? employeeRepo.findByIdAndBusiness(dto.getEmployeeId(), business)
                .orElse(null);

        Order order = new Order();
        order.setBusiness(business);
        order.setCustomer(customer);
        order.setEmployee(employee);
        order.setDate(LocalDateTime.now());
        order.setAmount(0);
        Order savedOrder = orderRepo.save(order);

        double totalAmount = 0;
        for (OrderDetailsDto detailDto : dto.getOrderDetails()) {
            Item item = itemRepo.findByItemIdAndBusiness(detailDto.getItemId(), business)
                    .orElseThrow(() -> new RuntimeException("Item not found"));
            if (item.getQuantity() < detailDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for item " + item.getName());
            }
            item.setQuantity(item.getQuantity() - detailDto.getQuantity());
            itemRepo.save(item);

            OrderDetails detail = new OrderDetails();
            detail.setOrder(savedOrder);
            detail.setItem(item);
            detail.setQuantity(detailDto.getQuantity());
            detail.setPrice(detailDto.getPrice());
            detail.setDiscount(detailDto.getDiscount());
            orderDetailsRepo.save(detail);

            totalAmount += (detailDto.getPrice() * detailDto.getQuantity()) - detailDto.getDiscount();
        }

        savedOrder.setAmount(totalAmount);
        savedOrder = orderRepo.save(savedOrder);

        return toDto(savedOrder);
    }

    @Override
    public List<OrderDto> getAllOrders() {
        Business business = currentBusiness();
        List<Order> all = orderRepo.findAllByBusiness(business);
        List<OrderDto> dtos = new ArrayList<>();
        for (Order o : all) {
            dtos.add(toDto(o));
        }
        return dtos;
    }

    @Override
    public OrderDto getOrderById(Long id) {
        Business business = currentBusiness();
        Optional<Order> byId = orderRepo.findById(id);
        if (byId.isPresent() && byId.get().getBusiness().equals(business)) {
            return toDto(byId.get());
        }
        return null;
    }

    @Override
    public OrderDto updateOrder(OrderDto dto) {
        // Implementation for update, including stock adjustment if changing quantity
        // For example, reverse old quantity, apply new
        // Omitted for now; add as needed
        return null;
    }

    @Override
    public OrderDto deleteOrder(Long id) {
        // Implementation for delete, restoring stock
        // For example, find order, add back quantity to items, delete
        // Omitted for now; add as needed
        return null;
    }
}