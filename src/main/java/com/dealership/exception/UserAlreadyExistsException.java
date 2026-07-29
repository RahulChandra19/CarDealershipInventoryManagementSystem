// src/main/java/com/dealership/exception/UserAlreadyExistsException.java
package com.dealership.exception;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) { super(message); }
}
