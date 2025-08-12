package lk.acpt.smartbiz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessResponse {
    private Long businessId;
    private Long ownerId;
    private String ownerEmail;
    private String businessName;
    private String address;
    private String status;
    private LocalDateTime registerDate;
}