package com.dealership.service;

import com.dealership.entity.*;
import com.dealership.exception.InsufficientStockException;
import com.dealership.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private VehicleRepository vehicleRepository;
    @Mock private UserRepository userRepository;
    @Mock private InventoryTransactionRepository transactionRepository;
    @Mock private OrderRepository orderRepository;
    @InjectMocks private InventoryServiceImpl inventoryService;

    private Vehicle vehicleWithStock(int quantity) {
        return Vehicle.builder().id(1L).make("Toyota").model("Camry")
                .price(new BigDecimal("28000")).quantity(quantity).isActive(true).build();
    }

    private User sampleUser() {
        return User.builder().id(1L).username("johndoe").role("CUSTOMER").build();
    }

    @Test
    void purchase_decreasesQuantityAndCreatesOrder_whenStockAvailable() {
        Vehicle vehicle = vehicleWithStock(5);
        User user = sampleUser();
        when(vehicleRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(vehicle));
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inventoryService.purchase(1L, 2, "johndoe");

        assertThat(vehicle.getQuantity()).isEqualTo(3);
        verify(orderRepository).save(any(Order.class));
        verify(transactionRepository).save(argThat(t ->
                t.getQuantityChange() == -2 && t.getType().equals("PURCHASE")));
    }

    @Test
    void purchase_throwsInsufficientStockException_whenStockInsufficient() {
        Vehicle vehicle = vehicleWithStock(1);
        when(vehicleRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> inventoryService.purchase(1L, 5, "johndoe"))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Only 1 unit(s) available");
    }

    @Test
    void purchase_throwsException_whenQuantityIsZero() {
        Vehicle vehicle = vehicleWithStock(0);
        when(vehicleRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(vehicle));

        assertThatThrownBy(() -> inventoryService.purchase(1L, 1, "johndoe"))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void restock_increasesQuantityAndLogsTransaction() {
        Vehicle vehicle = vehicleWithStock(3);
        User user = sampleUser();
        when(vehicleRepository.findByIdAndIsActiveTrue(1L)).thenReturn(Optional.of(vehicle));
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));
        when(vehicleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        inventoryService.restock(1L, 10, "johndoe");

        assertThat(vehicle.getQuantity()).isEqualTo(13);
        verify(transactionRepository).save(argThat(t ->
                t.getQuantityChange() == 10 && t.getType().equals("RESTOCK")));
    }
}