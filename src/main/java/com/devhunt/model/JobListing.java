package com.devhunt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_listings")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class JobListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String requirements;

    private Integer salaryMin;
    private Integer salaryMax;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JobType jobType = JobType.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JobStatus status = JobStatus.ACTIVE;

    @Builder.Default
    private boolean remote = true;

    @NotBlank
    private String category;

    @Column(columnDefinition = "TEXT")
    private String tags;

    // External source info (from parser)
    @Column(unique = true)
    private String externalId;

    private String externalUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime postedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @NotNull
    private Company company;

    @OneToMany(mappedBy = "jobListing", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JobApplication> applications = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (postedAt == null) postedAt = LocalDateTime.now();
    }

    public enum JobType {
        FULL_TIME("Full-time"),
        PART_TIME("Part-time"),
        CONTRACT("Contract"),
        FREELANCE("Freelance"),
        INTERNSHIP("Internship");

        private final String label;
        JobType(String label) { this.label = label; }
        public String getLabel() { return label; }
    }

    public enum JobStatus {
        ACTIVE, CLOSED, DRAFT
    }

    public String getSalaryRange() {
        if (salaryMin == null && salaryMax == null) return "Negotiable";
        if (salaryMin == null) return "Up to $" + salaryMax;
        if (salaryMax == null) return "From $" + salaryMin;
        return "$" + salaryMin + " – $" + salaryMax;
    }

    public int getApplicationCount() {
        return applications.size();
    }
}
