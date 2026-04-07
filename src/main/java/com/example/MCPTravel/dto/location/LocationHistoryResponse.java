package com.example.MCPTravel.dto.location;

import com.example.MCPTravel.entity.LocationHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationHistoryResponse {

    private Long id;
    private Long companyId;
    private String companyName;
    private Double latitude;
    private Double longitude;
    private String name;
    private String address;
    private String category;
    private LocalDateTime searchedAt;

    public static LocationHistoryResponse fromEntity(LocationHistory entity) {
        return LocationHistoryResponse.builder()
                .id(entity.getId())
                .companyId(entity.getCompany() != null ? entity.getCompany().getId() : null)
                .companyName(entity.getCompany() != null ? entity.getCompany().getName() : null)
                .latitude(entity.getLatitude())
                .longitude(entity.getLongitude())
                .name(entity.getName())
                .address(entity.getAddress())
                .category(entity.getCategory())
                .searchedAt(entity.getSearchedAt())
                .build();
    }
}
