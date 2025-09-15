package lk.acpt.smartbiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsageLogDto {
    private Long logId;
    private Long businessId;
    private String businessName;
    private Long userId;
    private String userEmail;
    private String action;
    private String details;
    private LocalDateTime timestamp;
    private String ipAddress;
}