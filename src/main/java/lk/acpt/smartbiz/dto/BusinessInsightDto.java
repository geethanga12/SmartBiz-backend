package lk.acpt.smartbiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessInsightDto {
    private String question;
    private String answer;
    private Map<String, Object> data;
    private String period;
    private String visualization; // For chart recommendations
}