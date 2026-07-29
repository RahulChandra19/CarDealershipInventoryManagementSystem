// src/main/java/com/dealership/dto/response/AuthResponse.java
package com.dealership.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private String token;
    private String username;
    private String role;
}
