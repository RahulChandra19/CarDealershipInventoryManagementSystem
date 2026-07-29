package com.dealership.service;

import com.dealership.entity.*;
import com.dealership.exception.InsufficientStockException;
import com.dealership.exception.VehicleNotFoundException;
import com.dealership.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void purchase(Long vehicleId, int quantity, String username) {
        Vehicle vehicle = vehicleRepository.findByIdAndIsActiveTrue(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        if (vehicle.getQuantity() < quantity) {
            throw new InsufficientStockException(
                    "Only " + vehicle.getQuantity() + " unit(s) available");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        vehicle.setQuantity(vehicle.getQuantity() - quantity);
        vehicleRepository.save(vehicle);

        orderRepository.save(Order.builder()
                .user(user)
                .vehicle(vehicle)
                .quantity(quantity)
                .totalPrice(vehicle.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .status("CONFIRMED")
                .build());

        transactionRepository.save(InventoryTransaction.builder()
                .vehicle(vehicle)
                .user(user)
                .type("PURCHASE")
                .quantityChange(-quantity)
                .priceAtTime(vehicle.getPrice())
                .build());
    }

    @Override
    @Transactional
    public void restock(Long vehicleId, int quantity, String username) {
        Vehicle vehicle = vehicleRepository.findByIdAndIsActiveTrue(vehicleId)
                .orElseThrow(() -> new VehicleNotFoundException(vehicleId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        vehicle.setQuantity(vehicle.getQuantity() + quantity);
        vehicleRepository.save(vehicle);

        transactionRepository.save(InventoryTransaction.builder()
                .vehicle(vehicle)
                .user(user)
                .type("RESTOCK")
                .quantityChange(quantity)
                .priceAtTime(vehicle.getPrice())
                .build());
    }

    @Override
    public List<InventoryTransaction> getTransactions(Long vehicleId) {
        return transactionRepository.findByVehicleIdOrderByCreatedAtDesc(vehicleId);
    }
}