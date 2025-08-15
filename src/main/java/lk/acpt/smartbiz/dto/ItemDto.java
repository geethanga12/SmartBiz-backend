package lk.acpt.smartbiz.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private Long supplierId;
    private String name;
    private String description;
    private double unitPrice;
    private int quantity;
}