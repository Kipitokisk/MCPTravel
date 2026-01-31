package com.example.MCPTravel.controller;

import com.example.MCPTravel.dto.company.CompanyResponse;
import com.example.MCPTravel.entity.CompanyStatus;
import com.example.MCPTravel.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discovery")
@RequiredArgsConstructor
@Tag(name = "Discovery API", description = "AI-readable endpoints for business discovery. No authentication required.")
public class DiscoveryController {

    private final CompanyService companyService;

    @Operation(
        summary = "Get available MCP tools",
        description = "Returns all available tools and their schemas. AI agents should call this first to understand what operations are available."
    )
    @ApiResponse(responseCode = "200", description = "List of available tools")
    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> getAvailableTools() {
        List<Map<String, Object>> tools = List.of(
            Map.of(
                "name", "search_companies",
                "description", "Search for businesses/companies by name, category, or status. Use this when user asks about finding restaurants, cafes, or other businesses.",
                "endpoint", "/api/discovery/companies",
                "method", "GET",
                "parameters", List.of(
                    Map.of("name", "category", "type", "string", "required", false,
                           "description", "Filter by business type (e.g., restaurant, cafe, bar)"),
                    Map.of("name", "name", "type", "string", "required", false,
                           "description", "Search by business name (partial match)"),
                    Map.of("name", "status", "type", "string", "required", false,
                           "description", "Filter by status: OPEN, CLOSED, BUSY, TEMPORARILY_CLOSED")
                )
            ),
            Map.of(
                "name", "find_nearby",
                "description", "Find businesses near a specific location. Use this when user asks about places 'near me' or near a specific address/coordinates.",
                "endpoint", "/api/discovery/nearby",
                "method", "GET",
                "parameters", List.of(
                    Map.of("name", "latitude", "type", "number", "required", true,
                           "description", "Latitude coordinate"),
                    Map.of("name", "longitude", "type", "number", "required", true,
                           "description", "Longitude coordinate"),
                    Map.of("name", "radiusKm", "type", "number", "required", false,
                           "description", "Search radius in kilometers (default: 5.0)"),
                    Map.of("name", "category", "type", "string", "required", false,
                           "description", "Filter by business type")
                )
            ),
            Map.of(
                "name", "get_company_details",
                "description", "Get full details about a specific company including menu, working hours, and current status.",
                "endpoint", "/api/discovery/companies/{id}",
                "method", "GET",
                "parameters", List.of(
                    Map.of("name", "id", "type", "integer", "required", true,
                           "description", "The company ID")
                )
            ),
            Map.of(
                "name", "get_categories",
                "description", "Get list of all available business categories. Use this to understand what types of businesses exist in the system.",
                "endpoint", "/api/discovery/categories",
                "method", "GET",
                "parameters", List.of()
            ),
            Map.of(
                "name", "get_open_now",
                "description", "Get businesses that are currently open based on their working hours. Use this when user asks about places open right now.",
                "endpoint", "/api/discovery/open-now",
                "method", "GET",
                "parameters", List.of(
                    Map.of("name", "category", "type", "string", "required", false,
                           "description", "Filter by business type")
                )
            ),
            Map.of(
                "name", "get_company_menu",
                "description", "Get the menu with prices for a specific company. Use this when user asks about menu items, prices, or what a restaurant serves.",
                "endpoint", "/api/companies/{companyId}/menu",
                "method", "GET",
                "parameters", List.of(
                    Map.of("name", "companyId", "type", "integer", "required", true,
                           "description", "The company ID"),
                    Map.of("name", "availableOnly", "type", "boolean", "required", false,
                           "description", "If true, returns only available items (default: false)")
                )
            )
        );

        return ResponseEntity.ok(Map.of(
            "mcp_version", "1.0",
            "service", "MCPTravel",
            "description", "Business discovery service for finding restaurants, cafes, and other businesses with real-time status",
            "tools", tools
        ));
    }

    @Operation(
        summary = "Search companies",
        description = "Search for businesses by name, category, or status. Returns a list of matching companies."
    )
    @ApiResponse(responseCode = "200", description = "List of companies matching the search criteria")
    @GetMapping("/companies")
    public ResponseEntity<Map<String, Object>> discoverCompanies(
            @Parameter(description = "Filter by business category (e.g., restaurant, cafe, bar)")
            @RequestParam(required = false) String category,
            @Parameter(description = "Search by business name (partial match)")
            @RequestParam(required = false) String name,
            @Parameter(description = "Filter by operational status")
            @RequestParam(required = false) CompanyStatus status
    ) {
        List<CompanyResponse> companies = companyService.searchCompanies(category, status, name);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", companies.size(),
                "data", companies,
                "filters_applied", Map.of(
                        "category", category != null ? category : "none",
                        "name", name != null ? name : "none",
                        "status", status != null ? status.name() : "none"
                )
        ));
    }

    @Operation(
        summary = "Find nearby companies",
        description = "Find businesses within a specified radius of given coordinates. Ideal for 'near me' queries."
    )
    @ApiResponse(responseCode = "200", description = "List of nearby companies sorted by distance")
    @GetMapping("/nearby")
    public ResponseEntity<Map<String, Object>> discoverNearby(
            @Parameter(description = "Latitude coordinate", required = true, example = "47.0105")
            @RequestParam Double latitude,
            @Parameter(description = "Longitude coordinate", required = true, example = "28.8638")
            @RequestParam Double longitude,
            @Parameter(description = "Search radius in kilometers", example = "5.0")
            @RequestParam(defaultValue = "5.0") Double radiusKm,
            @Parameter(description = "Filter by business category")
            @RequestParam(required = false) String category
    ) {
        List<CompanyResponse> companies = companyService.findNearbyCompanies(latitude, longitude, radiusKm);

        if (category != null && !category.isEmpty()) {
            companies = companies.stream()
                    .filter(c -> category.equalsIgnoreCase(c.getCategory()))
                    .toList();
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", companies.size(),
                "data", companies,
                "search_parameters", Map.of(
                        "latitude", latitude,
                        "longitude", longitude,
                        "radius_km", radiusKm,
                        "category", category != null ? category : "all"
                )
        ));
    }

    @Operation(
        summary = "Get company details",
        description = "Get full details about a specific company including menu, working hours, contact info, and current status."
    )
    @ApiResponse(responseCode = "200", description = "Company details")
    @ApiResponse(responseCode = "404", description = "Company not found")
    @GetMapping("/companies/{id}")
    public ResponseEntity<Map<String, Object>> getCompanyDetails(
            @Parameter(description = "Company ID", required = true)
            @PathVariable Long id
    ) {
        CompanyResponse company = companyService.getCompanyById(id);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", company
        ));
    }

    @Operation(
        summary = "Get all categories",
        description = "Returns a list of all available business categories in the system."
    )
    @ApiResponse(responseCode = "200", description = "List of categories")
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        List<CompanyResponse> allCompanies = companyService.getAllCompanies();

        List<String> categories = allCompanies.stream()
                .map(CompanyResponse::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted()
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "categories", categories,
                "count", categories.size()
        ));
    }

    @Operation(
        summary = "Get open businesses",
        description = "Returns businesses that are currently open based on their working hours. Uses real-time calculation."
    )
    @ApiResponse(responseCode = "200", description = "List of currently open businesses")
    @GetMapping("/open-now")
    public ResponseEntity<Map<String, Object>> getOpenCompanies(
            @Parameter(description = "Filter by business category")
            @RequestParam(required = false) String category
    ) {
        List<CompanyResponse> allCompanies = companyService.getAllCompanies();

        List<CompanyResponse> openCompanies = allCompanies.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsCurrentlyOpen()))
                .filter(c -> category == null || category.equalsIgnoreCase(c.getCategory()))
                .toList();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", openCompanies.size(),
                "data", openCompanies,
                "filter", Map.of(
                    "category", category != null ? category : "all",
                    "checked_at", java.time.LocalDateTime.now().toString()
                )
        ));
    }
}
