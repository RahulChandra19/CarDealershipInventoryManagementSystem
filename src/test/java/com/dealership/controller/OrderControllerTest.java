package com.dealership.controller;

import com.dealership.dto.response.OrderResponse;
import com.dealership.entity.Order;
import com.dealership.entity.User;
import com.dealership.entity.Vehicle;
import com.dealership.exception.OrderNotFoundException;
import com.dealership.exception.UnauthorizedException;
import com.dealership.security.JwtUtil;
import com.dealership.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private OrderService orderService;
    @MockBean private JwtUtil jwtUtil;

    // ── helpers ───────────────────────────────────────────────

    private Order sampleOrder() {
        User user = User.builder()
                .id(1L).username("johndoe").email("john@example.com").role("CUSTOMER")
                .passwordHash("hashed").build();
        Vehicle vehicle = Vehicle.builder()
                .id(1L).make("Toyota").model("Camry")
                .price(new BigDecimal("28000")).quantity(5).isActive(true).build();
        return Order.builder()
                .id(1L).user(user).vehicle(vehicle)
                .quantity(1).totalPrice(new BigDecimal("28000"))
                .status("CONFIRMED").build();
    }

    // ── GET /api/orders/me ────────────────────────────────────

    @Test
    @WithMockUser(username = "johndoe", roles = "CUSTOMER")
    void getMyOrders_returns200_withOrderList() throws Exception {
        when(orderService.getMyOrders("johndoe")).thenReturn(List.of(sampleOrder()));

        mockMvc.perform(get("/api/orders/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("CONFIRMED"))
                .andExpect(jsonPath("$[0].vehicleMake").value("Toyota"))
                .andExpect(jsonPath("$[0].username").value("johndoe"));
    }

    @Test
    @WithMockUser(username = "johndoe", roles = "CUSTOMER")
    void getMyOrders_returns200_withEmptyList_whenNoOrders() throws Exception {
        when(orderService.getMyOrders("johndoe")).thenReturn(List.of());

        mockMvc.perform(get("/api/orders/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getMyOrders_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/orders/me"))
                .andExpect(status().isUnauthorized());
    }

    // ── GET /api/orders ───────────────────────────────────────

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllOrders_returns200_forAdmin() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(sampleOrder()));

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].totalPrice").value(28000));
    }

    @Test
    @WithMockUser(username = "johndoe", roles = "CUSTOMER")
    void getAllOrders_returns403_forCustomer() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    // ── DELETE /api/orders/{id} ───────────────────────────────

    @Test
    @WithMockUser(username = "johndoe", roles = "CUSTOMER")
    void cancelOrder_returns200_whenOwnerCancels() throws Exception {
        doNothing().when(orderService).cancelOrder(1L, "johndoe", "ROLE_CUSTOMER");

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void cancelOrder_returns200_whenAdminCancels() throws Exception {
        doNothing().when(orderService).cancelOrder(1L, "admin", "ROLE_ADMIN");

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"));
    }

    @Test
    @WithMockUser(username = "janedoe", roles = "CUSTOMER")
    void cancelOrder_returns403_whenCustomerCancelsOtherUsersOrder() throws Exception {
        doThrow(new UnauthorizedException("You are not authorized to cancel this order"))
                .when(orderService).cancelOrder(1L, "janedoe", "ROLE_CUSTOMER");

        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("You are not authorized to cancel this order"));
    }

    @Test
    @WithMockUser(username = "johndoe", roles = "CUSTOMER")
    void cancelOrder_returns404_whenOrderDoesNotExist() throws Exception {
        doThrow(new OrderNotFoundException(99L))
                .when(orderService).cancelOrder(99L, "johndoe", "ROLE_CUSTOMER");

        mockMvc.perform(delete("/api/orders/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Order not found with id: 99"));
    }

    @Test
    void cancelOrder_returns401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/orders/1"))
                .andExpect(status().isUnauthorized());
    }
}