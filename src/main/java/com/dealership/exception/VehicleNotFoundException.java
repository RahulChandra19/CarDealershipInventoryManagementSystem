// src/main/java/com/dealership/exception/VehicleNotFoundException.java
package com.dealership.exception;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(Long id) {
        super("Vehicle not found with id: " + id);
    }
}
