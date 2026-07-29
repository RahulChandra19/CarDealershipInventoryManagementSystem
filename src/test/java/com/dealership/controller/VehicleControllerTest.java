// src/test/java/com/dealership/controller/VehicleControllerTest.java
package com.dealership.controller;

import com.dealership.dto.request.CreateVehicleRequest;
import com.dealership.dto.request.PatchVehicleRequest;
import com.dealership.dto.request.UpdateVehicleRequest;
import com.dealership.entity.Vehicle;
import com.dealership.exception.VehicleNotFoundException;
import com.dealership.security.JwtUtil;
import com.dealership.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VehicleController.class)
class VehicleControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private VehicleService vehicleService;
    @MockBean private JwtUtil jwtUtil;

    private Vehicle sampleVehicle() {
        return Vehicle.builder().id(1L).make("Toyota").model("Camry")
                .year(2023).category("SEDAN").price(new BigDecimal("28000"))
                .quantity(5).isActive(true).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createVehicle_returns201() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .make("Toyota").model("Camry").year(2023).category("SEDAN")
                .price(new BigDecimal("28000")).quantity(5).build();
        when(vehicleService.create(any(CreateVehicleRequest.class))).thenReturn(sampleVehicle());

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void createVehicle_returns403_forNonAdmin() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .make("Toyota").model("Camry").year(2023).category("SEDAN")
                .price(new BigDecimal("28000")).quantity(5).build();

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAllVehicles_returns200_withPage() throws Exception {
        Page<Vehicle> page = new PageImpl<>(List.of(sampleVehicle()), PageRequest.of(0, 10), 1);
        when(vehicleService.getAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/vehicles")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].make").value("Toyota"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getVehicleById_returns200() throws Exception {
        when(vehicleService.getById(1L)).thenReturn(sampleVehicle());

        mockMvc.perform(get("/api/vehicles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.make").value("Toyota"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getVehicleById_returns404_whenNotFound() throws Exception {
        when(vehicleService.getById(99L)).thenThrow(new VehicleNotFoundException(99L));

        mockMvc.perform(get("/api/vehicles/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Vehicle not found with id: 99"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void searchVehicles_returns200_withFilteredResults() throws Exception {
        when(vehicleService.search(eq("toyota"), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(sampleVehicle()));

        mockMvc.perform(get("/api/vehicles/search")
                        .param("make", "toyota"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].make").value("Toyota"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateVehicle_returns200() throws Exception {
        UpdateVehicleRequest request = UpdateVehicleRequest.builder()
                .make("Toyota").model("Camry").year(2024).category("SEDAN")
                .price(new BigDecimal("30000")).quantity(3).build();
        Vehicle updated = sampleVehicle();
        updated.setYear(2024);
        updated.setPrice(new BigDecimal("30000"));
        when(vehicleService.update(eq(1L), any(UpdateVehicleRequest.class))).thenReturn(updated);

        mockMvc.perform(put("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.price").value(30000));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void patchVehicle_returns200() throws Exception {
        PatchVehicleRequest request = PatchVehicleRequest.builder()
                .price(new BigDecimal("32000")).build();
        Vehicle patched = sampleVehicle();
        patched.setPrice(new BigDecimal("32000"));
        when(vehicleService.patch(eq(1L), any(PatchVehicleRequest.class))).thenReturn(patched);

        mockMvc.perform(patch("/api/vehicles/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(32000));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteVehicle_returns204() throws Exception {
        doNothing().when(vehicleService).softDelete(1L);

        mockMvc.perform(delete("/api/vehicles/1"))
                .andExpect(status().isNoContent());

        verify(vehicleService).softDelete(1L);
    }
}
