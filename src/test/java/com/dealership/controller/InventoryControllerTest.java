package com.dealership.controller;

import com.dealership.dto.request.PurchaseRequest;
import com.dealership.dto.request.RestockRequest;
import com.dealership.dto.response.TransactionResponse;
import com.dealership.entity.InventoryTransaction;
import com.dealership.entity.Vehicle;
import com.dealership.exception.InsufficientStockException;
import com.dealership.exception.VehicleNotFoundException;
import com.dealership.security.JwtUtil;
import com.dealership.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private InventoryService inventoryService;
    @MockBean private JwtUtil jwtUtil;

    // ── purchase ──────────────────────────────────────────────

    @Test
    @WithMockUser(username = "johndoe", roles = "CUSTOMER")
    void purchase_returns200_whenStockAvailable() throws Exception {
        PurchaseRequest request = PurchaseRequest.builder().quantity(2).build();
        doNothing().when(inventoryService).purchase(1L, 2, "johndoe");

        mockMvc.perform(post("/api/vehicles/1/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Purchase successful"));
    }

    @Test
    @WithMockUser(username = "johndoe", roles = "CUSTOMER")
    void purchase_returns409_whenInsufficientStock() throws Exception {
        PurchaseRequest request = PurchaseRequest.builder().quantity(99).build();
        doThrow(new InsufficientStockException("Only 1 unit(s) available"))
                .when(inventoryService).purchase(1L, 99, "johndoe");

        mockMvc.perform(post("/api/vehicles/1/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only 1 unit(s) available"));
    }

    @Test
    @WithMockUser(username = "johndoe", roles = "CUSTOMER")
    void purchase_returns400_whenQuantityIsZero() throws Exception {
        PurchaseRequest request = PurchaseRequest.builder().quantity(0).build();

        mockMvc.perform(post("/api/vehicles/1/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void purchase_returns401_whenNotAuthenticated() throws Exception {
        PurchaseRequest request = PurchaseRequest.builder().quantity(1).build();

        mockMvc.perform(post("/api/vehicles/1/purchase")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── restock ───────────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void restock_returns200_forAdmin() throws Exception {
        RestockRequest request = RestockRequest.builder().quantity(10).build();
        doNothing().when(inventoryService).restock(1L, 10, "admin");

        mockMvc.perform(post("/api/vehicles/1/restock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Restock successful"));
    }

    @Test
    @WithMockUser(username = "johndoe", roles = "CUSTOMER")
    void restock_returns403_forNonAdmin() throws Exception {
        RestockRequest request = RestockRequest.builder().quantity(10).build();

        mockMvc.perform(post("/api/vehicles/1/restock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ── transactions ──────────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getTransactions_returns200_withList() throws Exception {
        Vehicle vehicle = Vehicle.builder().id(1L).make("Toyota")
                .model("Camry").price(new BigDecimal("28000")).build();
        InventoryTransaction tx = InventoryTransaction.builder()
                .id(1L).vehicle(vehicle).type("PURCHASE")
                .quantityChange(-2).priceAtTime(new BigDecimal("28000"))
                .createdAt(LocalDateTime.now()).build();
        when(inventoryService.getTransactions(1L)).thenReturn(List.of(tx));

        mockMvc.perform(get("/api/vehicles/1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("PURCHASE"))
                .andExpect(jsonPath("$[0].quantityChange").value(-2));
    }

    @Test
    @WithMockUser(username = "johndoe", roles = "CUSTOMER")
    void getTransactions_returns403_forNonAdmin() throws Exception {
        mockMvc.perform(get("/api/vehicles/1/transactions"))
                .andExpect(status().isForbidden());
    }
}