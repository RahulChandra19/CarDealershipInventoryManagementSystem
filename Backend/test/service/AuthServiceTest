// src/test/java/com/dealership/service/AuthServiceTest.java
package com.dealership.service;

import com.dealership.dto.request.LoginRequest;
import com.dealership.dto.request.RegisterRequest;
import com.dealership.dto.response.AuthResponse;
import com.dealership.dto.response.UserResponse;
import com.dealership.entity.User;
import com.dealership.exception.InvalidCredentialsException;
import com.dealership.exception.UserAlreadyExistsException;
import com.dealership.repository.UserRepository;
import com.dealership.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @InjectMocks private AuthServiceImpl authService;

    @Test
    void register_succeeds_whenUsernameAndEmailAreUnique() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser").email("new@example.com").password("password123").build();
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtUtil.generateToken("newuser", "CUSTOMER")).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getUsername()).isEqualTo("newuser");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
        verify(passwordEncoder).encode("password123");
    }

    @Test
    void register_throwsUserAlreadyExistsException_whenUsernameIsTaken() {
        RegisterRequest request = RegisterRequest.builder()
                .username("taken").email("new@example.com").password("password123").build();
        when(userRepository.existsByUsername("taken")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Username already taken");
    }

    @Test
    void register_throwsUserAlreadyExistsException_whenEmailIsTaken() {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser").email("taken@example.com").password("password123").build();
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    void login_returnsToken_whenCredentialsAreValid() {
        LoginRequest request = LoginRequest.builder()
                .username("johndoe").password("password123").build();
        User user = User.builder().id(1L).username("johndoe")
                .passwordHash("hashed").role("CUSTOMER").build();
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(jwtUtil.generateToken("johndoe", "CUSTOMER")).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
    }

    @Test
    void login_throwsInvalidCredentialsException_whenPasswordIsWrong() {
        LoginRequest request = LoginRequest.builder()
                .username("johndoe").password("wrongpassword").build();
        User user = User.builder().username("johndoe")
                .passwordHash("hashed").role("CUSTOMER").build();
        when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongpassword", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Invalid credentials");
    }

    @Test
    void login_throwsInvalidCredentialsException_whenUserDoesNotExist() {
        LoginRequest request = LoginRequest.builder()
                .username("nobody").password("password123").build();
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
