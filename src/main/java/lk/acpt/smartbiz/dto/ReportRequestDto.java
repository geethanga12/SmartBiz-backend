package lk.acpt.smartbiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDto {
    private String reportType; // SALES, INVENTORY, PROFIT_LOSS, CUSTOMER_ANALYSIS
    private LocalDate startDate;
    private LocalDate endDate;
    private String format; // PDF, JSON
    private boolean includeDetails;
}