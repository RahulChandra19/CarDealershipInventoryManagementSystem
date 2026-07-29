// src/test/java/com/dealership/service/VehicleServiceTest.java
package com.dealership.service;

import com.dealership.dto.request.CreateVehicleRequest;
import com.dealership.dto.request.PatchVehicleRequest;
import com.dealership.entity.Vehicle;
import com.dealership.exception.VehicleNotFoundException;
import com.dealership.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock private VehicleRepository vehicleRepository;
    @InjectMocks private VehicleServiceImpl vehicleService;

    private Vehicle sampleVehicle() {
        return Vehicle.builder().id(1L).make("Toyota").model("Camry")
                .year(2023).category("SEDAN").price(new BigDecimal("28000"))
                .quantity(5).isActive(true).build();
    }

    @Test
    void createVehicle_savesAndReturnsVehicle() {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .make("Toyota").model("Camry").year(2023).category("SEDAN")
                .price(new BigDecimal("28000")).quantity(5).build();
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(sampleVehicle());

        Vehicle result = vehicleService.create(request);

        assertThat(result.getMake()).isEqualTo("Toyota");
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void getAllVehicles_returnsPaginatedList() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Vehicle> page = new PageImpl<>(List.of(sampleVehicle()), pageable, 1);
        when(vehicleRepository.findAllByIsActiveTrue(pageable)).thenReturn(page);

        Page<Vehicle> result = vehicleService.getAll(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getVehicleById_returnsVehicle_whenExists() {
        when(vehicleRepository.findByIdAndIsActiveTrue(1L))
                .thenReturn(Optional.of(sampleVehicle()));

        Vehicle result = vehicleService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getVehicleById_throwsVehicleNotFoundException_whenNotFound() {
        when(vehicleRepository.findByIdAndIsActiveTrue(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleService.getById(99L))
                .isInstanceOf(VehicleNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void softDeleteVehicle_setsIsActiveFalse() {
        Vehicle vehicle = sampleVehicle();
        when(vehicleRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        vehicleService.softDelete(1L);

        verify(vehicleRepository).save(argThat(v -> !v.getIsActive()));
    }

    @Test
    void patchVehicle_updatesOnlyProvidedFields() {
        Vehicle vehicle = sampleVehicle();
        when(vehicleRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        PatchVehicleRequest patch = PatchVehicleRequest.builder()
                .price(new BigDecimal("30000")).build();

        Vehicle result = vehicleService.patch(1L, patch);

        assertThat(result.getPrice()).isEqualByComparingTo("30000");
        assertThat(result.getMake()).isEqualTo("Toyota");
    }
}
