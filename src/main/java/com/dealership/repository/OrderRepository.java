// src/main/java/com/dealership/repository/OrderRepository.java
package com.dealership.repository;

import com.dealership.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findAllByOrderByCreatedAtDesc();
    Optional<Order> findByIdAndUserId(Long id, Long userId);
}
