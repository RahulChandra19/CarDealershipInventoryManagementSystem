package com.dealership.integration;

import com.dealership.dto.request.CreateVehicleRequest;
import com.dealership.dto.request.PurchaseRequest;
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

class OrderIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;
    private String customerToken;
    private String otherCustomerToken;

    // ── setup ─────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws Exception {
        if (!userRepository.existsByUsername("orderadmin")) {
            userRepository.save(User.builder()
                    .username("orderadmin")
                    .email("orderadmin@example.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role("ADMIN")
                    .build());
        }
        adminToken = loginAndGetToken("orderadmin", "admin123");

        if (!userRepository.existsByUsername("ordercustomer")) {
            registerAndGetToken("ordercustomer",
                    "ordercustomer@example.com", "password123");
        }
        customerToken = loginAndGetToken("ordercustomer", "password123");

        if (!userRepository.existsByUsername("othercustomer")) {
            registerAndGetToken("othercustomer",
                    "othercustomer@example.com", "password123");
        }
        otherCustomerToken = loginAndGetToken("othercustomer", "password123");
    }

    // ── GET /api/orders/me ────────────────────────────────────

    @Test
    void getMyOrders_returns200_withOrdersAfterPurchase() throws Exception {
        Long vehicleId = createVehicleAsAdmin("Lexus", "RX", "SUV", "55000", 5);
        purchaseAsCustomer(vehicleId, 1, customerToken);

        mockMvc.perform(get("/api/orders/me")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].vehicleMake").value("Lexus"))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].quantity").value(1));
    }

    @Test
    void getMyOrders_returns200_withEmptyList_whenNoPurchasesMade() throws Exception {
        // fresh token from a user who hasn't bought anything
        String freshToken = registerAndGetToken(
                "freshuser", "fresh@example.com", "password123");

        mockMvc.perform(get("/api/orders/me")
                        .header("Authorization", "Bearer " + freshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMyOrders_doesNotReturnOtherUsersOrders() throws Exception {
        Long vehicleId = createVehicleAsAdmin("Volvo", "XC90", "SUV", "60000", 5);

        // customer buys, other customer buys separately
        purchaseAsCustomer(vehicleId, 1, customerToken);
        purchaseAsCustomer(vehicleId, 1, otherCustomerToken);

        // each user should only see their own order
        mockMvc.perform(get("/api/orders/me")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/orders/me")
                        .header("Authorization", "Bearer " + otherCustomerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET /api/orders (admin) ───────────────────────────────

    @Test
    void getAllOrders_returns200_withAllOrders_whenAdmin() throws Exception {
        Long vehicleId = createVehicleAsAdmin("Audi", "Q7", "SUV", "70000", 5);
        purchaseAsCustomer(vehicleId, 1, customerToken);
        purchaseAsCustomer(vehicleId, 1, otherCustomerToken);

        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllOrders_returns403_whenCustomer() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/orders/{id} ───────────────────────────────

    @Test
    void cancelOrder_returns200_andRestocksVehicle_whenOwnerCancels() throws Exception {
        Long vehicleId = createVehicleAsAdmin("Mercedes", "GLE", "SUV", "75000", 5);
        purchaseAsCustomer(vehicleId, 2, customerToken);

        // get the order id from /me
        var result = mockMvc.perform(get("/api/orders/me")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andReturn();

        Long orderId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get(0).get("id").asLong();

        // cancel it
        mockMvc.perform(delete("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"));

        // vehicle quantity should be restocked (5 - 2 + 2 = 5 again)
        mockMvc.perform(get("/api/vehicles/" + vehicleId)
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void cancelOrder_returns200_whenAdminCancelsAnyOrder() throws Exception {
        Long vehicleId = createVehicleAsAdmin("Genesis", "GV80", "SUV", "65000", 3);
        purchaseAsCustomer(vehicleId, 1, customerToken);

        var result = mockMvc.perform(get("/api/orders/me")
                        .header("Authorization", "Bearer " + customerToken))
                .andReturn();

        Long orderId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get(0).get("id").asLong();

        // admin cancels the customer's order
        mockMvc.perform(delete("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"));
    }

    @Test
    void cancelOrder_returns403_whenCustomerCancelsOtherUsersOrder() throws Exception {
        Long vehicleId = createVehicleAsAdmin("Lincoln", "Navigator", "SUV", "80000", 3);
        purchaseAsCustomer(vehicleId, 1, customerToken);

        var result = mockMvc.perform(get("/api/orders/me")
                        .header("Authorization", "Bearer " + customerToken))
                .andReturn();

        Long orderId = objectMapper.readTree(
                result.getResponse().getContentAsString()).get(0).get("id").asLong();

        // other customer tries to cancel an order that doesn't belong to them
        mockMvc.perform(delete("/api/orders/" + orderId)
                        .header("Authorization", "Bearer " + otherCustomerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(
                        "You are not authorized to cancel this order"));
    }

    @Test
    void cancelOrder_returns404_whenOrderDoesNotExist() throws Exception {
        mockMvc.perform(delete("/api/orders/999999")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isNotFound());
    }

    // ── private helpers ───────────────────────────────────────

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

        return objectMapper.readTree(
                result.getResponse().getContentAsString()).get("id").asLong();
    }

    private void purchaseAsCustomer(Long vehicleId,
                                    int quantity,
                                    String token) throws Exception {
        PurchaseRequest request = PurchaseRequest.builder()
                .quantity(quantity).build();

        mockMvc.perform(post("/api/vehicles/" + vehicleId + "/purchase")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}