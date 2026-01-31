package com.example.MCPTravel.dto.company;

import com.example.MCPTravel.dto.menu.MenuItemResponse;
import com.example.MCPTravel.entity.Company;
import com.example.MCPTravel.entity.CompanyStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class CompanyResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phoneNumber;
    private String website;
    private String category;
    private Map<String, String> workingHours;
    private List<MenuItemResponse> menuItems;
    private String specialEvents;
    private CompanyStatus status;
    private Boolean isCurrentlyOpen;
    private Integer warningCount;
    private String ownerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CompanyResponse fromEntity(Company company) {
        List<MenuItemResponse> menuItemResponses = company.getMenuItems() != null
                ? company.getMenuItems().stream()
                    .map(MenuItemResponse::fromEntity)
                    .collect(Collectors.toList())
                : List.of();

        return CompanyResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .description(company.getDescription())
                .address(company.getAddress())
                .latitude(company.getLatitude())
                .longitude(company.getLongitude())
                .phoneNumber(company.getPhoneNumber())
                .website(company.getWebsite())
                .category(company.getCategory())
                .workingHours(company.getWorkingHours())
                .menuItems(menuItemResponses)
                .specialEvents(company.getSpecialEvents())
                .status(company.getStatus())
                .isCurrentlyOpen(company.isCurrentlyOpen())
                .warningCount(company.getWarningCount())
                .ownerUsername(company.getOwner().getUsername())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
