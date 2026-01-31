package com.example.MCPTravel.repository;

import com.example.MCPTravel.entity.Company;
import com.example.MCPTravel.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByCompany(Company company);
    List<MenuItem> findByCompanyId(Long companyId);
    List<MenuItem> findByCompanyIdAndIsAvailableTrue(Long companyId);
    List<MenuItem> findByCompanyIdAndCategory(Long companyId, String category);
}
