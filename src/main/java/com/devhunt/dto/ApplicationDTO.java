package com.devhunt.dto;

import com.devhunt.model.JobApplication;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

public class ApplicationDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotBlank(message = "Name is required")
        private String applicantName;

        @Email(message = "Valid email is required")
        @NotBlank
        private String email;

        private String phone;
        private String coverLetter;
        private String resumeUrl;

        @NotNull(message = "Job listing is required")
        private Long jobListingId;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class StatusUpdate {
        @NotNull
        private JobApplication.ApplicationStatus status;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String applicantName;
        private String email;
        private String phone;
        private String coverLetter;
        private String resumeUrl;
        private JobApplication.ApplicationStatus status;
        private LocalDateTime appliedAt;
        private LocalDateTime reviewedAt;

        // Job info
        private Long jobListingId;
        private String jobTitle;
        private String companyName;

        public static Response from(JobApplication app) {
            return Response.builder()
                    .id(app.getId())
                    .applicantName(app.getApplicantName())
                    .email(app.getEmail())
                    .phone(app.getPhone())
                    .coverLetter(app.getCoverLetter())
                    .resumeUrl(app.getResumeUrl())
                    .status(app.getStatus())
                    .appliedAt(app.getAppliedAt())
                    .reviewedAt(app.getReviewedAt())
                    .jobListingId(app.getJobListing().getId())
                    .jobTitle(app.getJobListing().getTitle())
                    .companyName(app.getJobListing().getCompany().getName())
                    .build();
        }
    }
}
