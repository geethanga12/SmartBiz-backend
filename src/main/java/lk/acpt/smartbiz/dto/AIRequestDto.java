package lk.acpt.smartbiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRequestDto {
    private String type; // BUSINESS_INSIGHTS, EMAIL_GENERATOR, MARKETING_POST, INVOICE_SUMMARY
    private String prompt;
    private Object context; // Additional context data (can be Map, List, etc.)
}