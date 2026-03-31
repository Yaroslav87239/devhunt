package com.devhunt.controller;

import com.devhunt.dto.JobListingDTO;
import com.devhunt.model.JobListing;
import com.devhunt.service.JobListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobListingRestController {

    private final JobListingService jobListingService;

    @GetMapping
    public ResponseEntity<List<JobListingDTO.Response>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(defaultValue = "false") boolean activeOnly) {

        List<JobListingDTO.Response> jobs;

        if (search != null && !search.isBlank()) {
            jobs = jobListingService.search(search);
        } else if (Boolean.TRUE.equals(remote)) {
            jobs = jobListingService.findRemote();
        } else if (activeOnly) {
            jobs = jobListingService.findActive();
        } else {
            jobs = jobListingService.findAll();
        }

        if (category != null && !category.isBlank()) {
            String cat = category.toLowerCase();
            jobs = jobs.stream()
                    .filter(j -> j.getCategory() != null && j.getCategory().toLowerCase().contains(cat))
                    .toList();
        }

        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobListingDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(jobListingService.findById(id));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<JobListingDTO.Response>> getByCompany(@PathVariable Long companyId) {
        return ResponseEntity.ok(jobListingService.findByCompany(companyId));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(jobListingService.getAllCategories());
    }

    @PostMapping
    public ResponseEntity<JobListingDTO.Response> create(@Valid @RequestBody JobListingDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobListingService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobListingDTO.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody JobListingDTO.Request dto) {
        return ResponseEntity.ok(jobListingService.update(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobListingDTO.Response> updateStatus(
            @PathVariable Long id,
            @RequestParam JobListing.JobStatus status) {
        return ResponseEntity.ok(jobListingService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        jobListingService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Job listing deleted successfully"));
    }
}
