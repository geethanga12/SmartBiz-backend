package lk.acpt.smartbiz.controller;

import lk.acpt.smartbiz.dto.OrderDto;
import lk.acpt.smartbiz.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/order")
@PreAuthorize("hasRole('OWNER')")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto dto) {
        OrderDto saved = orderService.saveOrder(dto);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getOrder(@PathVariable Long id) {
        OrderDto order = orderService.getOrderById(id);
        if (order != null) return new ResponseEntity<>(order, HttpStatus.OK);
        return new ResponseEntity<>(Map.of("result", "Order not found"), HttpStatus.NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        return new ResponseEntity<>(orderService.getAllOrders(), HttpStatus.OK);
    }

    // inside OrderController (or add to your existing OrderController)
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable("id") Long id, Authentication auth) {
        try {
            byte[] pdf = orderService.generateInvoicePdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "invoice_" + id + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}