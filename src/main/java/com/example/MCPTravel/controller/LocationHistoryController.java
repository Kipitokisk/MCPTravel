package com.example.MCPTravel.controller;

import com.example.MCPTravel.dto.location.LocationHistoryRequest;
import com.example.MCPTravel.dto.location.LocationHistoryResponse;
import com.example.MCPTravel.service.LocationHistoryService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/users/locations")
@RequiredArgsConstructor
@Tag(name = "Location History", description = "Manage user's saved/searched locations")
@SecurityRequirement(name = "bearerAuth")
public class LocationHistoryController {

    private final LocationHistoryService locationHistoryService;

    @Operation(
        summary = "Get location history",
        description = "Returns all saved locations for the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "List of saved locations")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getHistory() {
        List<LocationHistoryResponse> history = locationHistoryService.getUserHistory();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "count", history.size(),
            "data", history
        ));
    }

    @Operation(
        summary = "Get recent locations",
        description = "Returns the most recent saved locations"
    )
    @ApiResponse(responseCode = "200", description = "List of recent locations")
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentHistory(
            @Parameter(description = "Maximum number of locations to return")
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<LocationHistoryResponse> history = locationHistoryService.getRecentHistory(limit);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "count", history.size(),
            "data", history
        ));
    }

    @Operation(
        summary = "Get locations by category",
        description = "Returns saved locations filtered by category"
    )
    @ApiResponse(responseCode = "200", description = "List of locations in category")
    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getHistoryByCategory(
            @Parameter(description = "Category to filter by")
            @PathVariable String category
    ) {
        List<LocationHistoryResponse> history = locationHistoryService.getHistoryByCategory(category);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "count", history.size(),
            "category", category,
            "data", history
        ));
    }

    @Operation(
        summary = "Save a location",
        description = "Saves a location to the user's history"
    )
    @ApiResponse(responseCode = "200", description = "Location saved successfully")
    @PostMapping
    public ResponseEntity<Map<String, Object>> saveLocation(
            @Valid @RequestBody LocationHistoryRequest request
    ) {
        LocationHistoryResponse saved = locationHistoryService.saveLocation(request);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Location saved to history",
            "data", saved
        ));
    }

    @Operation(
        summary = "Delete a location",
        description = "Removes a location from the user's history"
    )
    @ApiResponse(responseCode = "200", description = "Location deleted successfully")
    @ApiResponse(responseCode = "404", description = "Location not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteLocation(
            @Parameter(description = "Location ID to delete")
            @PathVariable Long id
    ) {
        locationHistoryService.deleteLocation(id);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Location removed from history"
        ));
    }

    @Operation(
        summary = "Clear all history",
        description = "Removes all locations from the user's history"
    )
    @ApiResponse(responseCode = "200", description = "History cleared successfully")
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearHistory() {
        locationHistoryService.clearHistory();
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Location history cleared"
        ));
    }
}
