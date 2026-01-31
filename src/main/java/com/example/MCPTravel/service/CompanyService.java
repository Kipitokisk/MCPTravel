package com.example.MCPTravel.service;

import com.example.MCPTravel.dto.company.CompanyRequest;
import com.example.MCPTravel.dto.company.CompanyResponse;
import com.example.MCPTravel.entity.Company;
import com.example.MCPTravel.entity.CompanyStatus;
import com.example.MCPTravel.entity.Role;
import com.example.MCPTravel.entity.User;
import com.example.MCPTravel.repository.CompanyRepository;
import com.example.MCPTravel.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public List<CompanyResponse> getAllCompanies() {
        return companyRepository.findAll().stream()
                .map(CompanyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public CompanyResponse getCompanyById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        return CompanyResponse.fromEntity(company);
    }

    public List<CompanyResponse> getMyCompanies() {
        User owner = getCurrentUser();
        return companyRepository.findByOwner(owner).stream()
                .map(CompanyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CompanyResponse createCompany(CompanyRequest request) {
        User owner = getCurrentUser();

        if (owner.getRole() != Role.BUSINESS_OWNER && owner.getRole() != Role.ADMIN) {
            throw new RuntimeException("Only business owners can create companies");
        }

        Company company = Company.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phoneNumber(request.getPhoneNumber())
                .website(request.getWebsite())
                .category(request.getCategory())
                .workingHours(request.getWorkingHours())
                .menu(request.getMenu())
                .specialEvents(request.getSpecialEvents())
                .status(request.getStatus() != null ? request.getStatus() : CompanyStatus.OPEN)
                .owner(owner)
                .build();

        company = companyRepository.save(company);
        return CompanyResponse.fromEntity(company);
    }

    @Transactional
    public CompanyResponse updateCompany(Long id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        User currentUser = getCurrentUser();
        if (!company.getOwner().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("You can only update your own companies");
        }

        if (request.getName() != null) company.setName(request.getName());
        if (request.getDescription() != null) company.setDescription(request.getDescription());
        if (request.getAddress() != null) company.setAddress(request.getAddress());
        if (request.getLatitude() != null) company.setLatitude(request.getLatitude());
        if (request.getLongitude() != null) company.setLongitude(request.getLongitude());
        if (request.getPhoneNumber() != null) company.setPhoneNumber(request.getPhoneNumber());
        if (request.getWebsite() != null) company.setWebsite(request.getWebsite());
        if (request.getCategory() != null) company.setCategory(request.getCategory());
        if (request.getWorkingHours() != null) company.setWorkingHours(request.getWorkingHours());
        if (request.getMenu() != null) company.setMenu(request.getMenu());
        if (request.getSpecialEvents() != null) company.setSpecialEvents(request.getSpecialEvents());
        if (request.getStatus() != null) company.setStatus(request.getStatus());

        company = companyRepository.save(company);
        return CompanyResponse.fromEntity(company);
    }

    @Transactional
    public void deleteCompany(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        User currentUser = getCurrentUser();
        if (!company.getOwner().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("You can only delete your own companies");
        }

        companyRepository.delete(company);
    }

    @Transactional
    public CompanyResponse updateStatus(Long id, CompanyStatus status) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        User currentUser = getCurrentUser();
        if (!company.getOwner().getId().equals(currentUser.getId()) && currentUser.getRole() != Role.ADMIN) {
            throw new RuntimeException("You can only update your own companies");
        }

        company.setStatus(status);
        company = companyRepository.save(company);
        return CompanyResponse.fromEntity(company);
    }

    public List<CompanyResponse> searchCompanies(String category, CompanyStatus status, String name) {
        return companyRepository.searchCompanies(category, status, name).stream()
                .map(CompanyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<CompanyResponse> findNearbyCompanies(Double latitude, Double longitude, Double radiusKm) {
        return companyRepository.findNearbyCompanies(latitude, longitude, radiusKm).stream()
                .map(CompanyResponse::fromEntity)
                .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
