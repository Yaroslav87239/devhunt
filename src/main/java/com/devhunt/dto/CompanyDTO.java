package com.devhunt.dto;

import com.devhunt.model.Company;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

public class CompanyDTO {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Request {
        @NotBlank(message = "Company name is required")
        private String name;
        private String website;
        private String description;
        private String location;
        private String logoUrl;
        @NotBlank(message = "Industry is required")
        private String industry;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private String name;
        private String website;
        private String description;
        private String location;
        private String logoUrl;
        private String industry;
        private int activeJobCount;
        private LocalDateTime createdAt;

        public static Response from(Company company) {
            return Response.builder()
                    .id(company.getId())
                    .name(company.getName())
                    .website(company.getWebsite())
                    .description(company.getDescription())
                    .location(company.getLocation())
                    .logoUrl(company.getLogoUrl())
                    .industry(company.getIndustry())
                    .activeJobCount(company.getActiveJobCount())
                    .createdAt(company.getCreatedAt())
                    .build();
        }
    }
}
