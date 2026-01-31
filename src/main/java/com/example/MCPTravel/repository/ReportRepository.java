package com.example.MCPTravel.repository;

import com.example.MCPTravel.entity.Company;
import com.example.MCPTravel.entity.Report;
import com.example.MCPTravel.entity.ReportStatus;
import com.example.MCPTravel.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByReporter(User reporter);
    List<Report> findByCompany(Company company);
    List<Report> findByStatus(ReportStatus status);
    List<Report> findByCompanyAndStatus(Company company, ReportStatus status);
    long countByCompanyAndStatus(Company company, ReportStatus status);
}
