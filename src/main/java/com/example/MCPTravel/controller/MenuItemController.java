package com.example.MCPTravel.controller;

import com.example.MCPTravel.dto.menu.MenuItemRequest;
import com.example.MCPTravel.dto.menu.MenuItemResponse;
import com.example.MCPTravel.service.MenuItemService;
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
@RequestMapping("/api/companies/{companyId}/menu")
@RequiredArgsConstructor
@Tag(name = "Menu Items", description = "Menu management for companies")
public class MenuItemController {

    private final MenuItemService menuItemService;

    @Operation(summary = "Get company menu", description = "Get all menu items for a company")
    @ApiResponse(responseCode = "200", description = "List of menu items")
    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getMenu(
            @Parameter(description = "Company ID") @PathVariable Long companyId,
            @Parameter(description = "Only available items") @RequestParam(defaultValue = "false") boolean availableOnly
    ) {
        if (availableOnly) {
            return ResponseEntity.ok(menuItemService.getAvailableMenuByCompanyId(companyId));
        }
        return ResponseEntity.ok(menuItemService.getMenuByCompanyId(companyId));
    }

    @Operation(summary = "Get menu item", description = "Get a specific menu item by ID")
    @GetMapping("/items/{itemId}")
    public ResponseEntity<MenuItemResponse> getMenuItem(@PathVariable Long companyId, @PathVariable Long itemId) {
        return ResponseEntity.ok(menuItemService.getMenuItemById(itemId));
    }

    @Operation(
        summary = "Add menu item",
        description = "Add a single item to the company menu. Requires ownership.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "200", description = "Menu item created")
    @ApiResponse(responseCode = "403", description = "Not the company owner")
    @PostMapping
    public ResponseEntity<MenuItemResponse> addMenuItem(
            @PathVariable Long companyId,
            @Valid @RequestBody MenuItemRequest request
    ) {
        return ResponseEntity.ok(menuItemService.createMenuItem(companyId, request));
    }

    @Operation(
        summary = "Add multiple menu items",
        description = "Bulk add items to the company menu. Requires ownership.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping("/bulk")
    public ResponseEntity<List<MenuItemResponse>> addMenuItems(
            @PathVariable Long companyId,
            @Valid @RequestBody List<MenuItemRequest> requests
    ) {
        return ResponseEntity.ok(menuItemService.createMenuItems(companyId, requests));
    }

    @Operation(
        summary = "Update menu item",
        description = "Update an existing menu item. Requires ownership.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/items/{itemId}")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long companyId,
            @PathVariable Long itemId,
            @Valid @RequestBody MenuItemRequest request
    ) {
        return ResponseEntity.ok(menuItemService.updateMenuItem(itemId, request));
    }

    @Operation(
        summary = "Delete menu item",
        description = "Delete a menu item. Requires ownership.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponse(responseCode = "204", description = "Menu item deleted")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long companyId, @PathVariable Long itemId) {
        menuItemService.deleteMenuItem(itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Clear menu",
        description = "Delete all menu items for a company. Requires ownership.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping
    public ResponseEntity<Void> clearMenu(@PathVariable Long companyId) {
        menuItemService.deleteAllMenuItems(companyId);
        return ResponseEntity.noContent().build();
    }
}
