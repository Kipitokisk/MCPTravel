package com.example.MCPTravel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "companies")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String address;

    private Double latitude;

    private Double longitude;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String website;

    private String category;

    @ElementCollection
    @CollectionTable(name = "company_working_hours", joinColumns = @JoinColumn(name = "company_id"))
    @MapKeyColumn(name = "day_of_week")
    @Column(name = "hours")
    @Builder.Default
    private Map<String, String> workingHours = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "company_menu_items", joinColumns = @JoinColumn(name = "company_id"))
    @Column(name = "menu_item")
    @Builder.Default
    private List<String> menu = new ArrayList<>();

    @Column(name = "special_events", length = 2000)
    private String specialEvents;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CompanyStatus status = CompanyStatus.OPEN;

    @Column(name = "warning_count")
    @Builder.Default
    private Integer warningCount = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Report> reports = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
