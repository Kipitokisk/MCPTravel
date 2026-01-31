package com.example.MCPTravel.dto.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReportRequest {
    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotBlank(message = "Description is required")
    private String description;

    private String proofUrl;
}
