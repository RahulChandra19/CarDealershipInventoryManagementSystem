package com.dealership.dto.response;

import com.dealership.entity.Order;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderResponse {

    private Long id;
    private Long userId;
    private String username;
    private Long vehicleId;
    private String vehicleMake;
    private String vehicleModel;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String status;
    private LocalDateTime createdAt;

    public static OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .username(order.getUser().getUsername())
                .vehicleId(order.getVehicle().getId())
                .vehicleMake(order.getVehicle().getMake())
                .vehicleModel(order.getVehicle().getModel())
                .quantity(order.getQuantity())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .build();
    }
}