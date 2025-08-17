package lk.acpt.smartbiz.entity;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private double unitPrice; // Selling price

    @Column(nullable = false)
    private double costPrice; // New: Purchase cost for profits

    @Column(nullable = false)
    private int quantity;
}