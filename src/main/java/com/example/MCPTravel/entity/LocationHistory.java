package com.example.MCPTravel.entity;

import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.time.LocalDateTime;

@Entity
@Table(name = "location_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Column(nullable = false)
    private String name;

    private String address;

    private String category;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    @PrePersist
    protected void onCreate() {
        searchedAt = LocalDateTime.now();
        updateLocation();
    }

    private void updateLocation() {
        if (latitude != null && longitude != null) {
            this.location = geometryFactory.createPoint(new Coordinate(longitude, latitude));
        }
    }
}
