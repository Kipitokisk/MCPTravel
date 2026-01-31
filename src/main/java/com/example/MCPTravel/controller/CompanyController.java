package com.example.MCPTravel.controller;

import com.example.MCPTravel.dto.company.CompanyRequest;
import com.example.MCPTravel.dto.company.CompanyResponse;
import com.example.MCPTravel.entity.CompanyStatus;
import com.example.MCPTravel.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "Companies", description = "Business/Company management endpoints")
public class CompanyController {

    private final CompanyService companyService;

    @Operation(summary = "Get all companies", description = "Returns a list of all registered companies")
    @ApiResponse(responseCode = "200", description = "List of companies")
    @GetMapping
    public ResponseEntity<List<CompanyResponse>> getAllCompanies() {
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @Operation(summary = "Get company by ID", description = "Returns details of a specific company")
    @ApiResponse(responseCode = "200", description = "Company details")
    @ApiResponse(responseCode = "404", description = "Company not found")
    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @Operation(
        summary = "Get my companies",
        description = "Returns companies owned by the authenticated user",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "List of owned companies")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @GetMapping("/my")
    public ResponseEntity<List<CompanyResponse>> getMyCompanies() {
        return ResponseEntity.ok(companyService.getMyCompanies());
    }

    @Operation(summary = "Search companies", description = "Search companies by category, status, or name")
    @GetMapping("/search")
    public ResponseEntity<List<CompanyResponse>> searchCompanies(
            @Parameter(description = "Business category") @RequestParam(required = false) String category,
            @Parameter(description = "Company status") @RequestParam(required = false) CompanyStatus status,
            @Parameter(description = "Company name (partial match)") @RequestParam(required = false) String name
    ) {
        return ResponseEntity.ok(companyService.searchCompanies(category, status, name));
    }

    @Operation(summary = "Find nearby companies", description = "Find companies within a radius of given coordinates")
    @GetMapping("/nearby")
    public ResponseEntity<List<CompanyResponse>> findNearbyCompanies(
            @Parameter(description = "Latitude", required = true) @RequestParam Double latitude,
            @Parameter(description = "Longitude", required = true) @RequestParam Double longitude,
            @Parameter(description = "Radius in km") @RequestParam(defaultValue = "5.0") Double radiusKm
    ) {
        return ResponseEntity.ok(companyService.findNearbyCompanies(latitude, longitude, radiusKm));
    }

    @Operation(
        summary = "Create a new company",
        description = "Register a new business. Requires BUSINESS_OWNER or ADMIN role.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Company created successfully")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @ApiResponse(responseCode = "403", description = "Not authorized (not a business owner)")
    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.createCompany(request));
    }

    @Operation(
        summary = "Update company",
        description = "Update an existing company. Only the owner or admin can update.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Company updated successfully")
    @ApiResponse(responseCode = "403", description = "Not authorized")
    @ApiResponse(responseCode = "404", description = "Company not found")
    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(
            @PathVariable Long id,
            @Valid @RequestBody CompanyRequest request
    ) {
        return ResponseEntity.ok(companyService.updateCompany(id, request));
    }

    @Operation(
        summary = "Update company status",
        description = "Quickly update operational status (OPEN, CLOSED, BUSY, etc.)",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PatchMapping("/{id}/status")
    public ResponseEntity<CompanyResponse> updateStatus(
            @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam CompanyStatus status
    ) {
        return ResponseEntity.ok(companyService.updateStatus(id, status));
    }

    @Operation(
        summary = "Delete company",
        description = "Delete a company. Only the owner or admin can delete.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "204", description = "Company deleted")
    @ApiResponse(responseCode = "403", description = "Not authorized")
    @ApiResponse(responseCode = "404", description = "Company not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }
}
