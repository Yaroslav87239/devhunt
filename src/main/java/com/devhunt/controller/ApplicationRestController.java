package com.devhunt.controller;

import com.devhunt.dto.ApplicationDTO;
import com.devhunt.model.JobApplication;
import com.devhunt.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationRestController {

    private final ApplicationService applicationService;

    @GetMapping
    public ResponseEntity<List<ApplicationDTO.Response>> getAll(
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String email) {

        if (jobId != null) return ResponseEntity.ok(applicationService.findByJob(jobId));
        if (email != null && !email.isBlank()) return ResponseEntity.ok(applicationService.findByEmail(email));
        return ResponseEntity.ok(applicationService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ApplicationDTO.Response> apply(
            @Valid @RequestBody ApplicationDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(applicationService.apply(dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationDTO.Response> updateStatus(
            @PathVariable Long id,
            @RequestParam JobApplication.ApplicationStatus status) {
        return ResponseEntity.ok(applicationService.updateStatus(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        applicationService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Application withdrawn successfully"));
    }
}
