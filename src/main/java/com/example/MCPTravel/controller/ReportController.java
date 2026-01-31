package com.example.MCPTravel.controller;

import com.example.MCPTravel.dto.report.ReportRequest;
import com.example.MCPTravel.dto.report.ReportResponse;
import com.example.MCPTravel.dto.report.ReportReviewRequest;
import com.example.MCPTravel.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "Reports", description = "Report management for flagging businesses with incorrect information")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Get all reports", description = "Admin only: Get all submitted reports")
    @ApiResponse(responseCode = "200", description = "List of all reports")
    @ApiResponse(responseCode = "403", description = "Not an admin")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getAllReports() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @Operation(summary = "Get pending reports", description = "Admin only: Get reports awaiting review")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportResponse>> getPendingReports() {
        return ResponseEntity.ok(reportService.getPendingReports());
    }

    @Operation(summary = "Get report by ID", description = "Get details of a specific report")
    @ApiResponse(responseCode = "200", description = "Report details")
    @ApiResponse(responseCode = "404", description = "Report not found")
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponse> getReportById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @Operation(summary = "Get my reports", description = "Get reports submitted by the authenticated user")
    @GetMapping("/my")
    public ResponseEntity<List<ReportResponse>> getMyReports() {
        return ResponseEntity.ok(reportService.getMyReports());
    }

    @Operation(
        summary = "Create a report",
        description = "Report a business for incorrect or misleading information. Attach proof URL if available."
    )
    @ApiResponse(responseCode = "200", description = "Report created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @PostMapping
    public ResponseEntity<ReportResponse> createReport(@Valid @RequestBody ReportRequest request) {
        return ResponseEntity.ok(reportService.createReport(request));
    }

    @Operation(
        summary = "Review a report",
        description = "Admin only: Approve or deny a report. Approved reports add warnings to the business."
    )
    @ApiResponse(responseCode = "200", description = "Report reviewed")
    @ApiResponse(responseCode = "403", description = "Not an admin")
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReportResponse> reviewReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportReviewRequest request
    ) {
        return ResponseEntity.ok(reportService.reviewReport(id, request));
    }

    @Operation(summary = "Delete a report", description = "Delete a report. Users can only delete their own reports.")
    @ApiResponse(responseCode = "204", description = "Report deleted")
    @ApiResponse(responseCode = "403", description = "Not authorized")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }
}
