package lk.acpt.smartbiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIResponseDto {
    private boolean success;
    private String response;
    private String type;
    private int tokensUsed;
    private String errorMessage;
}