// src/main/java/com/dealership/exception/OrderNotFoundException.java
package com.dealership.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Order not found with id: " + id);
    }
}
