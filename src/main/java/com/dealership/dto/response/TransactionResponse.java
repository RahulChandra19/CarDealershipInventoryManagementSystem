package com.dealership.dto.response;

import com.dealership.entity.InventoryTransaction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransactionResponse {

    private Long id;
    private Long vehicleId;
    private Long userId;
    private String type;
    private Integer quantityChange;
    private BigDecimal priceAtTime;
    private LocalDateTime createdAt;

    public static TransactionResponse fromEntity(InventoryTransaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .vehicleId(t.getVehicle().getId())
                .userId(t.getUser() != null ? t.getUser().getId() : null)
                .type(t.getType())
                .quantityChange(t.getQuantityChange())
                .priceAtTime(t.getPriceAtTime())
                .createdAt(t.getCreatedAt())
                .build();
    }
}