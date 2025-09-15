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
public class AIUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long aiUsageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String requestType; // BUSINESS_INSIGHTS, EMAIL_GENERATOR, etc.

    @Column(columnDefinition = "TEXT")
    private String prompt;

    @Column(columnDefinition = "TEXT")
    private String response;

    private LocalDateTime requestTime;
    private int tokensUsed;
    private double cost; // For future billing
    private boolean successful;
    private String errorMessage;
}