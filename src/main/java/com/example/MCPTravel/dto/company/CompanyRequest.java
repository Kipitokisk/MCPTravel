package com.example.MCPTravel.dto.company;

import com.example.MCPTravel.entity.CompanyStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CompanyRequest {
    @NotBlank(message = "Company name is required")
    private String name;

    private String description;

    @NotBlank(message = "Address is required")
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
}
