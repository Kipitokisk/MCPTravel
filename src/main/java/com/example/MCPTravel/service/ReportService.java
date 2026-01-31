package com.example.MCPTravel.service;

import com.example.MCPTravel.dto.report.ReportRequest;
import com.example.MCPTravel.dto.report.ReportResponse;
import com.example.MCPTravel.dto.report.ReportReviewRequest;
import com.example.MCPTravel.entity.*;
import com.example.MCPTravel.repository.CompanyRepository;
import com.example.MCPTravel.repository.ReportRepository;
import com.example.MCPTravel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    private static final int WARNING_THRESHOLD = 3;

    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll().stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ReportResponse> getPendingReports() {
        return reportRepository.findByStatus(ReportStatus.PENDING).stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public ReportResponse getReportById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        return ReportResponse.fromEntity(report);
    }

    public List<ReportResponse> getMyReports() {
        User reporter = getCurrentUser();
        return reportRepository.findByReporter(reporter).stream()
                .map(ReportResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReportResponse createReport(ReportRequest request) {
        User reporter = getCurrentUser();
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new RuntimeException("Company not found"));

        Report report = Report.builder()
                .reporter(reporter)
                .company(company)
                .description(request.getDescription())
                .proofUrl(request.getProofUrl())
                .status(ReportStatus.PENDING)
                .build();

        report = reportRepository.save(report);
        return ReportResponse.fromEntity(report);
    }

    @Transactional
    public ReportResponse reviewReport(Long id, ReportReviewRequest request) {
        User admin = getCurrentUser();
        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only admins can review reports");
        }

        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        report.setStatus(request.getStatus());
        report.setAdminNotes(request.getAdminNotes());
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewedBy(admin);

        if (request.getStatus() == ReportStatus.APPROVED) {
            Company company = report.getCompany();
            company.setWarningCount(company.getWarningCount() + 1);

            if (company.getWarningCount() >= WARNING_THRESHOLD) {
                company.setStatus(CompanyStatus.TEMPORARILY_CLOSED);
            }

            companyRepository.save(company);
        }

        report = reportRepository.save(report);
        return ReportResponse.fromEntity(report);
    }

    @Transactional
    public void deleteReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found"));

        User currentUser = getCurrentUser();
        if (!report.getReporter().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("You can only delete your own reports");
        }

        reportRepository.delete(report);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
