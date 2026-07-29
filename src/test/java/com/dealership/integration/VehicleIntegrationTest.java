package com.dealership.integration;

import com.dealership.dto.request.CreateVehicleRequest;
import com.dealership.dto.request.PurchaseRequest;
import com.dealership.dto.request.UpdateVehicleRequest;
import com.dealership.entity.User;
import com.dealership.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VehicleIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String customerToken;

    // ── setup ─────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws Exception {
        // create admin directly in DB — register endpoint only creates CUSTOMERs
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(User.builder()
                    .username("admin")
                    .email("admin@example.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .build());
        }

        adminToken = loginAndGetToken("admin", "admin123");

        // customer registers normally through the API
        if (!userRepository.existsByUsername("customer")) {
            registerAndGetToken("customer", "customer@example.com", "password123");
        }
        customerToken = loginAndGetToken("customer", "password123");
    }

    // ── create vehicle (ADMIN) ────────────────────────────────

    @Test
    void createVehicle_returns201_whenAdmin() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .make("Toyota").model("Camry").year(2023)
                .category("SEDAN").price(new BigDecimal("28000"))
                .quantity(10).build();

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    void createVehicle_returns403_whenCustomer() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .make("Honda").model("Civic").year(2023)
                .category("SEDAN").price(new BigDecimal("22000"))
                .quantity(5).build();

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createVehicle_returns401_whenNotAuthenticated() throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .make("Honda").model("Civic").year(2023)
                .category("SEDAN").price(new BigDecimal("22000"))
                .quantity(5).build();

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ── list vehicles ─────────────────────────────────────────

    @Test
    void getVehicles_returns200_withPaginatedList() throws Exception {
        // seed a vehicle first
        createVehicleAsAdmin("Ford", "F-150", "TRUCK", "45000", 5);

        mockMvc.perform(get("/api/vehicles")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].make").exists());
    }

    @Test
    void getVehicles_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isUnauthorized());
    }

    // ── get by id ─────────────────────────────────────────────

    @Test
    void getVehicleById_returns200_whenVehicleExists() throws Exception {
        Long id = createVehicleAsAdmin("BMW", "X5", "SUV", "65000", 3);

        mockMvc.perform(get("/api/vehicles/" + id)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.make").value("BMW"));
    }

    @Test
    void getVehicleById_returns404_whenVehicleDoesNotExist() throws Exception {
        mockMvc.perform(get("/api/vehicles/999999")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(
                        "Vehicle not found with id: 999999"));
    }

    // ── search ────────────────────────────────────────────────

    @Test
    void searchVehicles_returnMatchingResults_byCategory() throws Exception {
        createVehicleAsAdmin("Chevrolet", "Silverado", "TRUCK", "50000", 4);
        createVehicleAsAdmin("Tesla", "Model 3", "SEDAN", "45000", 2);

        mockMvc.perform(get("/api/vehicles/search")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("category", "TRUCK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("TRUCK"));
    }

    @Test
    void searchVehicles_returnMatchingResults_byPriceRange() throws Exception {
        createVehicleAsAdmin("Toyota", "Corolla", "SEDAN", "20000", 6);
        createVehicleAsAdmin("Porsche", "911", "COUPE", "120000", 1);

        mockMvc.perform(get("/api/vehicles/search")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("minPrice", "10000")
                        .param("maxPrice", "50000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].make").value("Toyota"));
    }

    @Test
    void searchVehicles_returnsEmptyList_whenNoMatch() throws Exception {
        mockMvc.perform(get("/api/vehicles/search")
                        .header("Authorization", "Bearer " + customerToken)
                        .param("make", "Lamborghini"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // ── update (PUT) ──────────────────────────────────────────

    @Test
    void updateVehicle_returns200_andUpdatedFields_whenAdmin() throws Exception {
        Long id = createVehicleAsAdmin("Kia", "Stinger", "SEDAN", "35000", 3);

        UpdateVehicleRequest update = UpdateVehicleRequest.builder()
                .make("Kia").model("Stinger GT").year(2024)
                .category("SEDAN").price(new BigDecimal("37000"))
                .quantity(5).build();

        mockMvc.perform(put("/api/vehicles/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model").value("Stinger GT"))
                .andExpect(jsonPath("$.price").value(37000))
                .andExpect(jsonPath("$.quantity").value(5));
    }

    // ── soft delete ───────────────────────────────────────────

    @Test
    void deleteVehicle_returns204_andVehicleNoLongerVisible_whenAdmin() throws Exception {
        Long id = createVehicleAsAdmin("Mazda", "CX-5", "SUV", "30000", 4);

        // admin soft-deletes it
        mockMvc.perform(delete("/api/vehicles/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // should now return 404 since isActive=false
        mockMvc.perform(get("/api/vehicles/" + id)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteVehicle_returns403_whenCustomer() throws Exception {
        Long id = createVehicleAsAdmin("Mazda", "MX-5", "COUPE", "28000", 2);

        mockMvc.perform(delete("/api/vehicles/" + id)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    // ── purchase ──────────────────────────────────────────────

    @Test
    void purchase_returns200_andDecrementsQuantityInDB() throws Exception {
        Long id = createVehicleAsAdmin("Hyundai", "Tucson", "SUV", "28000", 5);

        PurchaseRequest purchase = PurchaseRequest.builder().quantity(2).build();

        mockMvc.perform(post("/api/vehicles/" + id + "/purchase")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchase)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Purchase successful"));

        // verify quantity actually decremented in the DB
        mockMvc.perform(get("/api/vehicles/" + id)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(3));
    }

    @Test
    void purchase_returns409_whenInsufficientStock() throws Exception {
        Long id = createVehicleAsAdmin("Nissan", "Leaf", "SEDAN", "35000", 1);

        PurchaseRequest purchase = PurchaseRequest.builder().quantity(10).build();

        mockMvc.perform(post("/api/vehicles/" + id + "/purchase")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(purchase)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Only 1 unit(s) available"));
    }

    // ── restock (ADMIN) ───────────────────────────────────────

    @Test
    void restock_returns200_andIncrementsQuantityInDB() throws Exception {
        Long id = createVehicleAsAdmin("Subaru", "Outback", "SUV", "32000", 2);

        mockMvc.perform(post("/api/vehicles/" + id + "/restock")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": 8}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Restock successful"));

        // verify quantity incremented
        mockMvc.perform(get("/api/vehicles/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(10));
    }

    // ── private helper ────────────────────────────────────────

    /**
     * Creates a vehicle as admin and returns its generated ID.
     * Used to seed data for test cases without copy-pasting the full HTTP call.
     */
    private Long createVehicleAsAdmin(String make, String model,
                                      String category, String price,
                                      int quantity) throws Exception {
        CreateVehicleRequest request = CreateVehicleRequest.builder()
                .make(make).model(model).year(2023)
                .category(category)
                .price(new BigDecimal(price))
                .quantity(quantity)
                .build();

        var result = mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }
}