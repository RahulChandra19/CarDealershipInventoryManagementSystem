// src/main/java/com/dealership/dto/request/PatchVehicleRequest.java
package com.dealership.dto.request;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PatchVehicleRequest {
    private String make;
    private String model;
    private Integer year;
    private String category;
    private BigDecimal price;
    private Integer quantity;
    private String description;
    private String imageUrl;
}
