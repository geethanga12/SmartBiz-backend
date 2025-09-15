package lk.acpt.smartbiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTemplateDto {
    private String to;
    private String subject;
    private String body;
    private String type; // THANK_YOU, FOLLOW_UP, COMPLAINT_RESPONSE, MARKETING
}