package com.dealership.service;

import com.dealership.entity.InventoryTransaction;

import java.util.List;

public interface InventoryService {
    void purchase(Long vehicleId, int quantity, String username);
    void restock(Long vehicleId, int quantity, String username);
    List<InventoryTransaction> getTransactions(Long vehicleId);
}