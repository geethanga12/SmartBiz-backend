package lk.acpt.smartbiz.service.impl;

import lk.acpt.smartbiz.dto.OrderDetailDto;
import lk.acpt.smartbiz.dto.OrderDto;
import lk.acpt.smartbiz.entity.*;
import lk.acpt.smartbiz.repo.*;
import lk.acpt.smartbiz.service.OrderService;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // New

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;
    private final OrderDetailRepository detailRepo;
    private final CustomerRepository customerRepo;
    private final EmployeeRepository employeeRepo;
    private final ItemRepository itemRepo;
    private final UserRepository userRepo;
    private final BusinessRepository businessRepo;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepo, OrderDetailRepository detailRepo, CustomerRepository customerRepo, EmployeeRepository employeeRepo, ItemRepository itemRepo, UserRepository userRepo, BusinessRepository businessRepo) {
        this.orderRepo = orderRepo;
        this.detailRepo = detailRepo;
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
        List<OrderDetailDto> details = new ArrayList<>();
        // Fetch details separately if needed; assume eager or query
        // For simplicity, assume Order has @OneToMany(mappedBy = "order") List<OrderDetail> details;
        // Add to entity if needed
        for (OrderDetail d : o.getDetails()) {
            details.add(new OrderDetailDto(d.getId(), d.getItem().getItemId(), d.getOrderItemQuantity(), d.getPrice(), d.getDiscount(), d.getSubtotal()));
        }

        return new OrderDto(
                o.getOrderId(),
                o.getCustomer().getCustomerId(),
                o.getEmployee() != null ? o.getEmployee().getEmployeeId() : null,
                o.getDate(),
                o.getAmount(),
                o.getStatus(),
                details
        );
    }

    @Override
    @Transactional
    public OrderDto saveOrder(OrderDto dto) {
        Business business = currentBusiness();

        Customer customer = customerRepo.findByCustomerIdAndBusiness(dto.getCustomerId(), business)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Employee employee = null;
        if (dto.getEmployeeId() != null) {
            employee = employeeRepo.findByEmployeeIdAndBusiness(dto.getEmployeeId(), business)
                    .orElseThrow(() -> new RuntimeException("Employee not found"));
        }

        Order order = new Order();
        order.setBusiness(business);
        order.setCustomer(customer);
        order.setEmployee(employee);
        order.setDate(LocalDateTime.now());
        order.setStatus("PAID"); // Default
        order.setAmount(0.0); // Calculate below

        Order savedOrder = orderRepo.save(order); // Save first to get ID

        double total = 0.0;
        List<OrderDetail> details = new ArrayList<>();
        for (OrderDetailDto detailDto : dto.getDetails()) {
            Item item = itemRepo.findByItemIdAndBusiness(detailDto.getItemId(), business)
                    .orElseThrow(() -> new RuntimeException("Item not found"));

            if (item.getQuantity() < detailDto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for item " + item.getName());
            }

            // Update stock
            item.setQuantity(item.getQuantity() - detailDto.getQuantity());
            itemRepo.save(item);

            double price = item.getUnitPrice(); // Snapshot
            double subtotal = detailDto.getQuantity() * price - detailDto.getDiscount();

            OrderDetail detail = new OrderDetail();
            detail.setOrder(savedOrder);
            detail.setItem(item);
            detail.setOrderItemQuantity(detailDto.getQuantity());
            detail.setPrice(price);
            detail.setDiscount(detailDto.getDiscount());
            detail.setSubtotal(subtotal);
            details.add(detailRepo.save(detail));

            total += subtotal;
        }

        savedOrder.setAmount(total);
        savedOrder = orderRepo.save(savedOrder); // Update total

        // Set details back if entity has list
        return toDto(savedOrder);
    }

    @Override
    public OrderDto getOrderById(Long id) {
        Business business = currentBusiness();
        return orderRepo.findById(id).filter(o -> o.getBusiness().equals(business)).map(this::toDto).orElse(null);
    }

    @Override
    public List<OrderDto> getAllOrders() {
        Business business = currentBusiness();
        return orderRepo.findAllByBusiness(business).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public byte[] generateInvoicePdf(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Build a simple invoice PDF with PDFBox (similar to ReportsServiceImpl)
        try (PDDocument doc = new PDDocument(); ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            doc.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(doc, page);

            float margin = 50;
            float y = page.getMediaBox().getHeight() - margin;
            float leading = 14f;

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA_BOLD, 16);
            cs.newLineAtOffset(margin, y);
            cs.showText("INVOICE #" + order.getOrderId());
            cs.endText();
            y -= leading * 1.5;

            cs.beginText();
            cs.setFont(PDType1Font.HELVETICA, 11);
            cs.newLineAtOffset(margin, y);
            cs.showText("Date: " + order.getDate().toLocalDate().toString());
            cs.endText();
            y -= leading;

            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText("Customer: " + (order.getCustomer() != null ? order.getCustomer().getCustomerName() : "N/A"));
            cs.endText();
            y -= leading * 1.5;

            // Table-like list of items
            cs.setFont(PDType1Font.HELVETICA, 10);
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.showText(String.format("%-20s %-8s %-10s %-10s", "Item", "Qty", "Unit", "Subtotal"));
            cs.endText();
            y -= leading;

            for (OrderDetail d : order.getDetails()) {
                if (y < 80) { cs.close(); page = new PDPage(PDRectangle.LETTER); doc.addPage(page); cs = new PDPageContentStream(doc, page); y = page.getMediaBox().getHeight() - margin; }
                String itemName = d.getItem() != null ? d.getItem().getName() : ("#"+d.getItem());
                String line = String.format("%-20s %-8d %-10.2f %-10.2f", itemName, d.getOrderItemQuantity(), d.getPrice(), d.getOrderItemQuantity() * d.getPrice());
                cs.beginText();
                cs.newLineAtOffset(margin, y);
                cs.showText(line);
                cs.endText();
                y -= leading;
            }

            y -= leading;
            cs.beginText();
            cs.newLineAtOffset(margin, y);
            cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
            cs.showText("Total: " + String.format("%.2f", order.getAmount()));
            cs.endText();
            cs.close();

            doc.save(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate invoice PDF: " + e.getMessage(), e);
        }
    }

}