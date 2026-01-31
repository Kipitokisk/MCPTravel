package com.example.MCPTravel.repository;

import com.example.MCPTravel.entity.Company;
import com.example.MCPTravel.entity.CompanyStatus;
import com.example.MCPTravel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    List<Company> findByOwner(User owner);

    List<Company> findByCategory(String category);

    List<Company> findByStatus(CompanyStatus status);

    List<Company> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM Company c WHERE " +
           "(:category IS NULL OR c.category = :category) AND " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Company> searchCompanies(
        @Param("category") String category,
        @Param("status") CompanyStatus status,
        @Param("name") String name
    );

    @Query(value = "SELECT *, " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(latitude)) * " +
           "cos(radians(longitude) - radians(:lng)) + sin(radians(:lat)) * " +
           "sin(radians(latitude)))) AS distance " +
           "FROM companies " +
           "WHERE latitude IS NOT NULL AND longitude IS NOT NULL " +
           "HAVING distance < :radius " +
           "ORDER BY distance",
           nativeQuery = true)
    List<Company> findNearbyCompanies(
        @Param("lat") Double latitude,
        @Param("lng") Double longitude,
        @Param("radius") Double radiusKm
    );

    @Query("SELECT c FROM Company c WHERE c.warningCount >= :count")
    List<Company> findByWarningCountGreaterThanEqual(@Param("count") Integer count);
}
