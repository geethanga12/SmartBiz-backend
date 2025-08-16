package lk.acpt.smartbiz.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailsDto {
    private Long itemId;
    private int quantity;
    private double price;
    private double discount;
}