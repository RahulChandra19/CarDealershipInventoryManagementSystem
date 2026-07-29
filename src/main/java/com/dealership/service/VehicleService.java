// src/main/java/com/dealership/service/VehicleService.java
package com.dealership.service;

import com.dealership.dto.request.CreateVehicleRequest;
import com.dealership.dto.request.PatchVehicleRequest;
import com.dealership.dto.request.UpdateVehicleRequest;
import com.dealership.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.util.List;

public interface VehicleService {
    Vehicle create(CreateVehicleRequest request);
    Page<Vehicle> getAll(Pageable pageable);
    Vehicle getById(Long id);
    List<Vehicle> search(String make, String model, String category,
                          BigDecimal minPrice, BigDecimal maxPrice);
    Vehicle update(Long id, UpdateVehicleRequest request);
    Vehicle patch(Long id, PatchVehicleRequest request);
    void softDelete(Long id);
}
