package com.example.MCPTravel.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

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

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MenuItem> menuItems = new ArrayList<>();

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

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        updateLocation();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateLocation();
    }

    private void updateLocation() {
        if (latitude != null && longitude != null) {
            this.location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        }
    }

    /**
     * Check if the company is currently open based on working hours.
     * Working hours format: "HH:mm-HH:mm" (e.g., "09:00-18:00")
     * Supports overnight hours (e.g., "22:00-02:00")
     */
    public boolean isCurrentlyOpen() {
        if (status == CompanyStatus.CLOSED || status == CompanyStatus.TEMPORARILY_CLOSED) {
            return false;
        }

        if (workingHours == null || workingHours.isEmpty()) {
            return status == CompanyStatus.OPEN;
        }

        LocalDateTime now = LocalDateTime.now();
        String dayKey = now.getDayOfWeek().name();
        String hours = workingHours.get(dayKey);

        if (hours == null || hours.isBlank() || hours.equalsIgnoreCase("closed")) {
            return false;
        }

        try {
            String[] parts = hours.split("-");
            if (parts.length != 2) {
                return status == CompanyStatus.OPEN;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime openTime = LocalTime.parse(parts[0].trim(), formatter);
            LocalTime closeTime = LocalTime.parse(parts[1].trim(), formatter);
            LocalTime currentTime = now.toLocalTime();

            if (closeTime.isAfter(openTime)) {
                // Normal hours (e.g., 09:00-18:00)
                return !currentTime.isBefore(openTime) && currentTime.isBefore(closeTime);
            } else {
                // Overnight hours (e.g., 22:00-02:00)
                return !currentTime.isBefore(openTime) || currentTime.isBefore(closeTime);
            }
        } catch (Exception e) {
            return status == CompanyStatus.OPEN;
        }
    }
}
