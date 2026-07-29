// src/main/java/com/dealership/repository/VehicleRepository.java
package com.dealership.repository;

import com.dealership.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Page<Vehicle> findAllByIsActiveTrue(Pageable pageable);

    Optional<Vehicle> findByIdAndIsActiveTrue(Long id);

    @Query("""
    SELECT v FROM Vehicle v WHERE v.isActive = true
    AND (:make IS NULL OR LOWER(v.make) LIKE LOWER(CONCAT('%', CAST(:make AS string), '%')))
    AND (:model IS NULL OR LOWER(v.model) LIKE LOWER(CONCAT('%', CAST(:model AS string), '%')))
    AND (:category IS NULL OR v.category = :category)
    AND (:minPrice IS NULL OR v.price >= :minPrice)
    AND (:maxPrice IS NULL OR v.price <= :maxPrice)
    """)
    List<Vehicle> searchVehicles(@Param("make") String make,
                                 @Param("model") String model,
                                 @Param("category") String category,
                                 @Param("minPrice") BigDecimal minPrice,
                                 @Param("maxPrice") BigDecimal maxPrice);

    List<String> findDistinctCategoryByIsActiveTrue();
}
