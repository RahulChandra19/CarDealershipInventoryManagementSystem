package com.dealership.controller;

import com.dealership.dto.response.OrderResponse;
import com.dealership.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // GET /api/orders/me — authenticated user sees their own orders
    @GetMapping("/me")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        List<OrderResponse> orders = orderService.getMyOrders(userDetails.getUsername())
                .stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orders);
    }

    // GET /api/orders — admin sees all orders
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        List<OrderResponse> orders = orderService.getAllOrders()
                .stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(orders);
    }

    // DELETE /api/orders/{id} — owner or admin can cancel
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        // pass the first granted authority (e.g. "ROLE_CUSTOMER" or "ROLE_ADMIN")
        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        orderService.cancelOrder(id, userDetails.getUsername(), role);

        return ResponseEntity.ok(Map.of("message", "Order cancelled successfully"));
    }
}