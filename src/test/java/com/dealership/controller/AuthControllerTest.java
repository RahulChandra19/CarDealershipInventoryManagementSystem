// src/test/java/com/dealership/controller/AuthControllerTest.java
package com.dealership.controller;

import com.dealership.dto.request.LoginRequest;
import com.dealership.dto.request.RegisterRequest;
import com.dealership.dto.response.AuthResponse;
import com.dealership.dto.response.UserResponse;
import com.dealership.exception.UserAlreadyExistsException;
import com.dealership.security.JwtUtil;
import com.dealership.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private AuthService authService;
    @MockBean private JwtUtil jwtUtil;

    @Test
    void register_returns201_withToken() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("johndoe").email("john@example.com").password("password123").build();
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token").username("johndoe").role("CUSTOMER").build();
        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void register_returns409_whenUsernameIsTaken() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("taken").email("new@example.com").password("password123").build();
        when(authService.register(any())).thenThrow(
                new UserAlreadyExistsException("Username already taken: taken"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username already taken: taken"));
    }

    @Test
    void register_returns400_whenRequestIsInvalid() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("").email("notanemail").password("short").build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns200_withToken() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .username("johndoe").password("password123").build();
        AuthResponse response = AuthResponse.builder()
                .token("jwt-token").username("johndoe").role("CUSTOMER").build();
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @WithMockUser(username = "johndoe", roles = "CUSTOMER")
    void me_returns200_withUserProfile() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L).username("johndoe").email("john@example.com").role("CUSTOMER").build();
        when(authService.getCurrentUser("johndoe")).thenReturn(response);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }
}
