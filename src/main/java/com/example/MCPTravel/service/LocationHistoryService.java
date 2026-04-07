package com.example.MCPTravel.service;

import com.example.MCPTravel.dto.location.LocationHistoryRequest;
import com.example.MCPTravel.dto.location.LocationHistoryResponse;
import com.example.MCPTravel.entity.Company;
import com.example.MCPTravel.entity.LocationHistory;
import com.example.MCPTravel.entity.User;
import com.example.MCPTravel.repository.CompanyRepository;
import com.example.MCPTravel.repository.LocationHistoryRepository;
import com.example.MCPTravel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LocationHistoryService {

    private final LocationHistoryRepository locationHistoryRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public List<LocationHistoryResponse> getUserHistory() {
        User user = getCurrentUser();
        return locationHistoryRepository.findByUserOrderBySearchedAtDesc(user).stream()
                .map(LocationHistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<LocationHistoryResponse> getRecentHistory(int limit) {
        User user = getCurrentUser();
        return locationHistoryRepository.findRecentByUser(user, limit).stream()
                .map(LocationHistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<LocationHistoryResponse> getHistoryByCategory(String category) {
        User user = getCurrentUser();
        return locationHistoryRepository.findByUserAndCategoryOrderBySearchedAtDesc(user, category).stream()
                .map(LocationHistoryResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public LocationHistoryResponse saveLocation(LocationHistoryRequest request) {
        User user = getCurrentUser();

        Company company = null;
        if (request.getCompanyId() != null) {
            company = companyRepository.findById(request.getCompanyId())
                    .orElse(null);
        }

        LocationHistory history = LocationHistory.builder()
                .user(user)
                .company(company)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .name(request.getName())
                .address(request.getAddress())
                .category(request.getCategory())
                .build();

        history = locationHistoryRepository.save(history);
        return LocationHistoryResponse.fromEntity(history);
    }

    @Transactional
    public void deleteLocation(Long id) {
        User user = getCurrentUser();
        LocationHistory history = locationHistoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        if (!history.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only delete your own locations");
        }

        locationHistoryRepository.delete(history);
    }

    @Transactional
    public void clearHistory() {
        User user = getCurrentUser();
        List<LocationHistory> history = locationHistoryRepository.findByUserOrderBySearchedAtDesc(user);
        locationHistoryRepository.deleteAll(history);
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
