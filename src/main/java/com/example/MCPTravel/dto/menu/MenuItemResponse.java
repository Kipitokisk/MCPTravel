package com.example.MCPTravel.dto.menu;

import com.example.MCPTravel.entity.MenuItem;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MenuItemResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private String imageUrl;
    private Boolean isAvailable;
    private Long companyId;

    public static MenuItemResponse fromEntity(MenuItem item) {
        return MenuItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .category(item.getCategory())
                .imageUrl(item.getImageUrl())
                .isAvailable(item.getIsAvailable())
                .companyId(item.getCompany().getId())
                .build();
    }
}
