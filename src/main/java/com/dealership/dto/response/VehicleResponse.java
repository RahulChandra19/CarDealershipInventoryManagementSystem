// src/main/java/com/dealership/dto/response/VehicleResponse.java
package com.dealership.dto.response;

import com.dealership.entity.Vehicle;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VehicleResponse {
    private Long id;
    private String make;
    private String model;
    private Integer year;
    private String category;
    private BigDecimal price;
    private Integer quantity;
    private String vin;
    private String description;
    private String imageUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;

    public static VehicleResponse fromEntity(Vehicle v) {
        return VehicleResponse.builder()
                .id(v.getId()).make(v.getMake()).model(v.getModel())
                .year(v.getYear()).category(v.getCategory()).price(v.getPrice())
                .quantity(v.getQuantity()).vin(v.getVin()).description(v.getDescription())
                .imageUrl(v.getImageUrl()).isActive(v.getIsActive())
                .createdAt(v.getCreatedAt()).build();
    }
}
