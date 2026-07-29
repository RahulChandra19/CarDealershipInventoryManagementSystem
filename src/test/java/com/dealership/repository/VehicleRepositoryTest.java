// src/test/java/com/dealership/repository/VehicleRepositoryTest.java
package com.dealership.repository;

import com.dealership.entity.Vehicle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class VehicleRepositoryTest {

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
    private VehicleRepository vehicleRepository;

    private Vehicle buildVehicle(String make, String model, String category,
                                  BigDecimal price, boolean active) {
        return Vehicle.builder()
                .make(make).model(model).year(2023)
                .category(category).price(price)
                .quantity(5).isActive(active).build();
    }

    @Test
    void findAllByIsActiveTrue_returnsOnlyActiveVehicles() {
        vehicleRepository.save(buildVehicle("Toyota", "Camry", "SEDAN",
                new BigDecimal("28000"), true));
        vehicleRepository.save(buildVehicle("Honda", "Civic", "SEDAN",
                new BigDecimal("22000"), false));

        Page<Vehicle> result = vehicleRepository.findAllByIsActiveTrue(
                PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getMake()).isEqualTo("Toyota");
    }

    @Test
    void searchVehicles_filtersByMakeAndCategory() {
        vehicleRepository.save(buildVehicle("Ford", "F-150", "TRUCK",
                new BigDecimal("45000"), true));
        vehicleRepository.save(buildVehicle("Toyota", "Camry", "SEDAN",
                new BigDecimal("28000"), true));

        List<Vehicle> result = vehicleRepository.searchVehicles(
                "ford", null, "TRUCK", null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getModel()).isEqualTo("F-150");
    }

    @Test
    void searchVehicles_filtersByPriceRange() {
        vehicleRepository.save(buildVehicle("Toyota", "Camry", "SEDAN",
                new BigDecimal("28000"), true));
        vehicleRepository.save(buildVehicle("BMW", "X5", "SUV",
                new BigDecimal("65000"), true));

        List<Vehicle> result = vehicleRepository.searchVehicles(
                null, null, null, new BigDecimal("20000"), new BigDecimal("40000"));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMake()).isEqualTo("Toyota");
    }
}
