package com.example.MCPTravel.repository;

import com.example.MCPTravel.entity.LocationHistory;
import com.example.MCPTravel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {

    List<LocationHistory> findByUserOrderBySearchedAtDesc(User user);

    List<LocationHistory> findByUserAndCategoryOrderBySearchedAtDesc(User user, String category);

    @Query("SELECT lh FROM LocationHistory lh WHERE lh.user = :user ORDER BY lh.searchedAt DESC LIMIT :limit")
    List<LocationHistory> findRecentByUser(@Param("user") User user, @Param("limit") int limit);

    void deleteByUserAndId(User user, Long id);

    boolean existsByUserAndCompanyId(User user, Long companyId);

    long countByUser(User user);
}
