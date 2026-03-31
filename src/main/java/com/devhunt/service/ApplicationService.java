package com.devhunt.service;

import com.devhunt.dto.ApplicationDTO;
import com.devhunt.model.JobApplication;
import com.devhunt.model.JobListing;
import com.devhunt.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobListingService jobListingService;

    public List<ApplicationDTO.Response> findAll() {
        return applicationRepository.findAll().stream()
                .map(ApplicationDTO.Response::from)
                .toList();
    }

    public ApplicationDTO.Response findById(Long id) {
        return applicationRepository.findById(id)
                .map(ApplicationDTO.Response::from)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
    }

    public List<ApplicationDTO.Response> findByJob(Long jobId) {
        return applicationRepository.findByJobListingId(jobId).stream()
                .map(ApplicationDTO.Response::from)
                .toList();
    }

    public List<ApplicationDTO.Response> findByEmail(String email) {
        return applicationRepository.findByEmailIgnoreCase(email).stream()
                .map(ApplicationDTO.Response::from)
                .toList();
    }

    public long countPending() {
        return applicationRepository.countPending();
    }

    @Transactional
    public ApplicationDTO.Response apply(ApplicationDTO.Request dto) {
        JobListing job = jobListingService.findEntityById(dto.getJobListingId());

        if (job.getStatus() != JobListing.JobStatus.ACTIVE) {
            throw new RuntimeException("This job is no longer accepting applications");
        }
        if (applicationRepository.existsByEmailAndJobListingId(dto.getEmail(), dto.getJobListingId())) {
            throw new RuntimeException("You have already applied to this job");
        }

        JobApplication application = JobApplication.builder()
                .applicantName(dto.getApplicantName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .coverLetter(dto.getCoverLetter())
                .resumeUrl(dto.getResumeUrl())
                .jobListing(job)
                .build();

        return ApplicationDTO.Response.from(applicationRepository.save(application));
    }

    @Transactional
    public ApplicationDTO.Response updateStatus(Long id, JobApplication.ApplicationStatus status) {
        JobApplication app = applicationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
        app.setStatus(status);
        app.setReviewedAt(LocalDateTime.now());
        return ApplicationDTO.Response.from(applicationRepository.save(app));
    }

    @Transactional
    public void delete(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new RuntimeException("Application not found with id: " + id);
        }
        applicationRepository.deleteById(id);
    }
}
