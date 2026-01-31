package com.example.MCPTravel.dto.report;

import com.example.MCPTravel.entity.Report;
import com.example.MCPTravel.entity.ReportStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ReportResponse {
    private Long id;
    private Long companyId;
    private String companyName;
    private String reporterUsername;
    private String description;
    private String proofUrl;
    private ReportStatus status;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String reviewedByUsername;

    public static ReportResponse fromEntity(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .companyId(report.getCompany().getId())
                .companyName(report.getCompany().getName())
                .reporterUsername(report.getReporter().getUsername())
                .description(report.getDescription())
                .proofUrl(report.getProofUrl())
                .status(report.getStatus())
                .adminNotes(report.getAdminNotes())
                .createdAt(report.getCreatedAt())
                .reviewedAt(report.getReviewedAt())
                .reviewedByUsername(report.getReviewedBy() != null ? report.getReviewedBy().getUsername() : null)
                .build();
    }
}
