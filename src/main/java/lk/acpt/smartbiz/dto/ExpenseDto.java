package lk.acpt.smartbiz.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseDto {
    private Long id;
    private LocalDateTime date;
    private double amount;
    private String description;
    private String category;
}