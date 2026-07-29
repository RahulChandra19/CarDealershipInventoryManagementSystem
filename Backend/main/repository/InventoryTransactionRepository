// src/main/java/com/dealership/repository/InventoryTransactionRepository.java
package com.dealership.repository;

import com.dealership.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findByVehicleIdOrderByCreatedAtDesc(Long vehicleId);
}
