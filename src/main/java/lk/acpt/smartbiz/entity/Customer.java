package lk.acpt.smartbiz.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false, unique = false) // unique per business (handled in queries)
    private String email;

    private String phone;
    private String address;
}
