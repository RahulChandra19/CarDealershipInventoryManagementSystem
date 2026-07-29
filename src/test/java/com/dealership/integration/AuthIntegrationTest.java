package com.dealership.integration;

import com.dealership.dto.request.LoginRequest;
import com.dealership.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthIntegrationTest extends BaseIntegrationTest {

    // ── register ──────────────────────────────────────────────

    @Test
    void register_returns201_andToken_withValidRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("authuser1")
                .email("authuser1@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("authuser1"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void register_returns409_whenUsernameAlreadyExists() throws Exception {
        // first registration succeeds
        registerAndGetToken("duplicateuser", "dup@example.com", "password123");

        // second registration with the same username should fail
        RegisterRequest duplicate = RegisterRequest.builder()
                .username("duplicateuser")
                .email("other@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Username already taken: duplicateuser"));
    }

    @Test
    void register_returns409_whenEmailAlreadyExists() throws Exception {
        registerAndGetToken("firstuser", "shared@example.com", "password123");

        RegisterRequest duplicate = RegisterRequest.builder()
                .username("seconduser")
                .email("shared@example.com")   // same email
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        "Email already registered: shared@example.com"));
    }

    @Test
    void register_returns400_whenRequestIsInvalid() throws Exception {
        RegisterRequest bad = RegisterRequest.builder()
                .username("")           // blank — fails @NotBlank
                .email("notanemail")    // fails @Email
                .password("short")     // fails @Size(min=8)
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());
    }

    // ── login ─────────────────────────────────────────────────

    @Test
    void login_returns200_andToken_withCorrectCredentials() throws Exception {
        registerAndGetToken("loginuser", "loginuser@example.com", "password123");

        LoginRequest login = LoginRequest.builder()
                .username("loginuser")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.username").value("loginuser"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void login_returns401_withWrongPassword() throws Exception {
        registerAndGetToken("wrongpassuser", "wrongpass@example.com", "password123");

        LoginRequest bad = LoginRequest.builder()
                .username("wrongpassuser")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    void login_returns401_whenUserDoesNotExist() throws Exception {
        LoginRequest bad = LoginRequest.builder()
                .username("nobody")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bad)))
                .andExpect(status().isUnauthorized());
    }

    // ── /me ───────────────────────────────────────────────────

    @Test
    void me_returns200_withUserProfile_whenTokenIsValid() throws Exception {
        String token = registerAndGetToken(
                "meuser", "meuser@example.com", "password123");

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("meuser"))
                .andExpect(jsonPath("$.email").value("meuser@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void me_returns401_withNoToken() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }
}