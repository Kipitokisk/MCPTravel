package com.example.MCPTravel.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "proof_url")
    private String proofUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @Column(name = "admin_notes", length = 1000)
    private String adminNotes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
