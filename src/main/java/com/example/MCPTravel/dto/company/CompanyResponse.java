package com.example.MCPTravel.dto.company;

import com.example.MCPTravel.entity.Company;
import com.example.MCPTravel.entity.CompanyStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    private List<String> menu;
    private String specialEvents;
    private CompanyStatus status;
    private Integer warningCount;
    private String ownerUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CompanyResponse fromEntity(Company company) {
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
                .menu(company.getMenu())
                .specialEvents(company.getSpecialEvents())
                .status(company.getStatus())
                .warningCount(company.getWarningCount())
                .ownerUsername(company.getOwner().getUsername())
                .createdAt(company.getCreatedAt())
                .updatedAt(company.getUpdatedAt())
                .build();
    }
}
