package com.dealership.service;

import com.dealership.entity.Order;

import java.util.List;

public interface OrderService {
    List<Order> getMyOrders(String username);
    List<Order> getAllOrders();
    void cancelOrder(Long orderId, String username, String role);
}