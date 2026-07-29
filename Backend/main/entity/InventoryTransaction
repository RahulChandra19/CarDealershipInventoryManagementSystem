// src/main/java/com/dealership/entity/InventoryTransaction.java
package com.dealership.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 20)
    private String type;       // PURCHASE or RESTOCK

    @Column(name = "quantity_change", nullable = false)
    private Integer quantityChange;   // negative for purchase, positive for restock

    @Column(name = "price_at_time", precision = 12, scale = 2)
    private BigDecimal priceAtTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
