package com.example.MCPTravel.dto.report;

import com.example.MCPTravel.entity.ReportStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportReviewRequest {
    @NotNull(message = "Status is required")
    private ReportStatus status;

    private String adminNotes;
}
