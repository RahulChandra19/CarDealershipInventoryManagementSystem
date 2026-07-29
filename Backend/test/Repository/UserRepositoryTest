// src/test/java/com/dealership/repository/UserRepositoryTest.java
package com.dealership.repository;

import com.dealership.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByUsername_returnsUser_whenUserExists() {
        User user = User.builder()
                .username("johndoe")
                .email("john@example.com")
                .passwordHash("hashed")
                .role("CUSTOMER")
                .build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("johndoe");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void existsByEmail_returnsTrue_whenEmailExists() {
        User user = User.builder()
                .username("janedoe")
                .email("jane@example.com")
                .passwordHash("hashed")
                .role("CUSTOMER")
                .build();
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("jane@example.com")).isTrue();
    }

    @Test
    void existsByUsername_returnsFalse_whenUserDoesNotExist() {
        assertThat(userRepository.existsByUsername("nobody")).isFalse();
    }
}
