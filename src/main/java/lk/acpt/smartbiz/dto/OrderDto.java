package lk.acpt.smartbiz.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private Long customerId;
    private Long employeeId;
    private LocalDateTime date;
    private double amount;
    private List<OrderDetailsDto> orderDetails;
}