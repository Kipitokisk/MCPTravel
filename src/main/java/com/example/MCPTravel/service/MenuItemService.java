package com.example.MCPTravel.service;

import com.example.MCPTravel.dto.menu.MenuItemRequest;
import com.example.MCPTravel.dto.menu.MenuItemResponse;
import com.example.MCPTravel.entity.Company;
import com.example.MCPTravel.entity.MenuItem;
import com.example.MCPTravel.entity.Role;
import com.example.MCPTravel.entity.User;
import com.example.MCPTravel.repository.CompanyRepository;
import com.example.MCPTravel.repository.MenuItemRepository;
import com.example.MCPTravel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public List<MenuItemResponse> getMenuByCompanyId(Long companyId) {
        return menuItemRepository.findByCompanyId(companyId).stream()
                .map(MenuItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<MenuItemResponse> getAvailableMenuByCompanyId(Long companyId) {
        return menuItemRepository.findByCompanyIdAndIsAvailableTrue(companyId).stream()
                .map(MenuItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public MenuItemResponse getMenuItemById(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));
        return MenuItemResponse.fromEntity(item);
    }

    @Transactional
    public MenuItemResponse createMenuItem(Long companyId, MenuItemRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        verifyOwnership(company);

        MenuItem item = MenuItem.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                .company(company)
                .build();

        item = menuItemRepository.save(item);
        return MenuItemResponse.fromEntity(item);
    }

    @Transactional
    public List<MenuItemResponse> createMenuItems(Long companyId, List<MenuItemRequest> requests) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        verifyOwnership(company);

        List<MenuItem> items = requests.stream()
                .map(request -> MenuItem.builder()
                        .name(request.getName())
                        .description(request.getDescription())
                        .price(request.getPrice())
                        .category(request.getCategory())
                        .imageUrl(request.getImageUrl())
                        .isAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true)
                        .company(company)
                        .build())
                .collect(Collectors.toList());

        items = menuItemRepository.saveAll(items);
        return items.stream()
                .map(MenuItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        verifyOwnership(item.getCompany());

        if (request.getName() != null) item.setName(request.getName());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getPrice() != null) item.setPrice(request.getPrice());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getImageUrl() != null) item.setImageUrl(request.getImageUrl());
        if (request.getIsAvailable() != null) item.setIsAvailable(request.getIsAvailable());

        item = menuItemRepository.save(item);
        return MenuItemResponse.fromEntity(item);
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu item not found"));

        verifyOwnership(item.getCompany());

        menuItemRepository.delete(item);
    }

    @Transactional
    public void deleteAllMenuItems(Long companyId) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        verifyOwnership(company);

        List<MenuItem> items = menuItemRepository.findByCompanyId(companyId);
        menuItemRepository.deleteAll(items);
    }

    private void verifyOwnership(Company company) {
        User currentUser = getCurrentUser();
        if (!company.getOwner().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("You can only manage menu for your own companies");
        }
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
