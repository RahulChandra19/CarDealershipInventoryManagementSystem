// src/main/java/com/dealership/service/VehicleServiceImpl.java
package com.dealership.service;

import com.dealership.dto.request.CreateVehicleRequest;
import com.dealership.dto.request.PatchVehicleRequest;
import com.dealership.dto.request.UpdateVehicleRequest;
import com.dealership.entity.Vehicle;
import com.dealership.exception.VehicleNotFoundException;
import com.dealership.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    @Override
    @Transactional
    public Vehicle create(CreateVehicleRequest request) {
        Vehicle vehicle = Vehicle.builder()
                .make(request.getMake()).model(request.getModel())
                .year(request.getYear()).category(request.getCategory())
                .price(request.getPrice()).quantity(request.getQuantity())
                .vin(request.getVin()).description(request.getDescription())
                .imageUrl(request.getImageUrl()).isActive(true).build();
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Page<Vehicle> getAll(Pageable pageable) {
        return vehicleRepository.findAllByIsActiveTrue(pageable);
    }

    @Override
    public Vehicle getById(Long id) {
        return vehicleRepository.findByIdAndIsActiveTrue(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));
    }

    @Override
    public List<Vehicle> search(String make, String model, String category,
                                 BigDecimal minPrice, BigDecimal maxPrice) {
        return vehicleRepository.searchVehicles(make, model, category, minPrice, maxPrice);
    }

    @Override
    @Transactional
    public Vehicle update(Long id, UpdateVehicleRequest request) {
        Vehicle vehicle = getById(id);
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setCategory(request.getCategory());
        vehicle.setPrice(request.getPrice());
        vehicle.setQuantity(request.getQuantity());
        vehicle.setVin(request.getVin());
        vehicle.setDescription(request.getDescription());
        vehicle.setImageUrl(request.getImageUrl());
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public Vehicle patch(Long id, PatchVehicleRequest request) {
        Vehicle vehicle = getById(id);
        if (request.getMake() != null)        vehicle.setMake(request.getMake());
        if (request.getModel() != null)       vehicle.setModel(request.getModel());
        if (request.getYear() != null)        vehicle.setYear(request.getYear());
        if (request.getCategory() != null)    vehicle.setCategory(request.getCategory());
        if (request.getPrice() != null)       vehicle.setPrice(request.getPrice());
        if (request.getQuantity() != null)    vehicle.setQuantity(request.getQuantity());
        if (request.getDescription() != null) vehicle.setDescription(request.getDescription());
        if (request.getImageUrl() != null)    vehicle.setImageUrl(request.getImageUrl());
        return vehicleRepository.save(vehicle);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Vehicle vehicle = getById(id);
        vehicle.setIsActive(false);
        vehicleRepository.save(vehicle);
    }
}
