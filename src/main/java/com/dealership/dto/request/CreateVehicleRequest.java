// src/main/java/com/dealership/dto/request/CreateVehicleRequest.java
package com.dealership.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateVehicleRequest {
    @NotBlank private String make;
    @NotBlank private String model;
    @NotNull @Min(1900) private Integer year;
    @NotBlank private String category;
    @NotNull @DecimalMin("0.0") private BigDecimal price;
    @NotNull @Min(0) private Integer quantity;
    private String vin;
    private String description;
    private String imageUrl;
}
