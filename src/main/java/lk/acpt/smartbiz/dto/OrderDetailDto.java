package lk.acpt.smartbiz.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDto {
    private Long id;
    private Long itemId;
    private int quantity;
    private double price;
    private double discount;
    private double subtotal;
}