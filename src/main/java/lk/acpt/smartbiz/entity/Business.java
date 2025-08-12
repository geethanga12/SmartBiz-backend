package lk.acpt.smartbiz.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long businessId;

    // owner -> USER.userId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    // optional subscription plan id (nullable for now)
    private Long subscriptionPlanId;

    private String businessName;
    private String address;
    private LocalDateTime registerDate;
    private String status; // e.g., PENDING, ACTIVE
}