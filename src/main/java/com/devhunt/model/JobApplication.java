package com.devhunt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_applications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class JobApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String applicantName;

    @Email
    @NotBlank
    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    private String resumeUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    private LocalDateTime reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_listing_id", nullable = false)
    private JobListing jobListing;

    @PrePersist
    protected void onCreate() {
        appliedAt = LocalDateTime.now();
    }

    public enum ApplicationStatus {
        PENDING("Pending"),
        REVIEWING("Reviewing"),
        SHORTLISTED("Shortlisted"),
        REJECTED("Rejected"),
        HIRED("Hired");

        private final String label;
        ApplicationStatus(String label) { this.label = label; }
        public String getLabel() { return label; }
    }
}
