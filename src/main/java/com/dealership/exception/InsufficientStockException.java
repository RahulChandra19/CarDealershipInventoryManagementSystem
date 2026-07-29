// src/main/java/com/dealership/exception/InsufficientStockException.java
package com.dealership.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) { super(message); }
}
