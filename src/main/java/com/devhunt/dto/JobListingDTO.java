package com.devhunt.dto;

import com.devhunt.model.JobListing;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class JobListingDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotBlank(message = "Job title is required")
        private String title;

        private String description;
        private String requirements;
        private Integer salaryMin;
        private Integer salaryMax;

        @NotNull(message = "Job type is required")
        private JobListing.JobType jobType;

        @NotBlank(message = "Category is required")
        private String category;

        private String tags;
        private boolean remote;

        @NotNull(message = "Company is required")
        private Long companyId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private String requirements;
        private String salaryRange;
        private JobListing.JobType jobType;
        private JobListing.JobStatus status;
        private boolean remote;
        private String category;
        private String tags;
        private String externalUrl;
        private int applicationCount;
        private LocalDateTime postedAt;

        // Company info (flattened for convenience)
        private Long companyId;
        private String companyName;
        private String companyLocation;
        private String companyLogoUrl;

        public static Response from(JobListing job) {
            return Response.builder()
                    .id(job.getId())
                    .title(job.getTitle())
                    .description(job.getDescription())
                    .requirements(job.getRequirements())
                    .salaryRange(job.getSalaryRange())
                    .jobType(job.getJobType())
                    .status(job.getStatus())
                    .remote(job.isRemote())
                    .category(job.getCategory())
                    .tags(job.getTags())
                    .externalUrl(job.getExternalUrl())
                    .applicationCount(job.getApplicationCount())
                    .postedAt(job.getPostedAt())
                    .companyId(job.getCompany().getId())
                    .companyName(job.getCompany().getName())
                    .companyLocation(job.getCompany().getLocation())
                    .companyLogoUrl(job.getCompany().getLogoUrl())
                    .build();
        }
    }
}
