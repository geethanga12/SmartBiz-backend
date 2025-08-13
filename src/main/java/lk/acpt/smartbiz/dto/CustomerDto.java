package lk.acpt.smartbiz.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String address;
}
