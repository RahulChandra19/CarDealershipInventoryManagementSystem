// src/main/java/com/dealership/controller/VehicleController.java
package com.dealership.controller;

import com.dealership.dto.request.CreateVehicleRequest;
import com.dealership.dto.request.PatchVehicleRequest;
import com.dealership.dto.request.UpdateVehicleRequest;
import com.dealership.dto.response.VehicleResponse;
import com.dealership.entity.Vehicle;
import com.dealership.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;

    @PostMapping
    public ResponseEntity<VehicleResponse> create(@Valid @RequestBody CreateVehicleRequest request) {
        Vehicle vehicle = vehicleService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(VehicleResponse.fromEntity(vehicle));
    }

    @GetMapping
    public ResponseEntity<Page<VehicleResponse>> getAll(Pageable pageable) {
        Page<VehicleResponse> page = vehicleService.getAll(pageable)
                .map(VehicleResponse::fromEntity);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getById(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.getById(id);
        return ResponseEntity.ok(VehicleResponse.fromEntity(vehicle));
    }

    @GetMapping("/search")
    public ResponseEntity<List<VehicleResponse>> search(
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        List<VehicleResponse> results = vehicleService
                .search(make, model, category, minPrice, maxPrice)
                .stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateVehicleRequest request) {
        Vehicle vehicle = vehicleService.update(id, request);
        return ResponseEntity.ok(VehicleResponse.fromEntity(vehicle));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<VehicleResponse> patch(@PathVariable Long id,
                                                  @RequestBody PatchVehicleRequest request) {
        Vehicle vehicle = vehicleService.patch(id, request);
        return ResponseEntity.ok(VehicleResponse.fromEntity(vehicle));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        vehicleService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
