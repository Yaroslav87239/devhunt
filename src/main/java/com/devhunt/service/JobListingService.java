package com.devhunt.service;

import com.devhunt.dto.JobListingDTO;
import com.devhunt.model.Company;
import com.devhunt.model.JobListing;
import com.devhunt.repository.JobListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobListingService {

    private final JobListingRepository jobListingRepository;
    private final CompanyService companyService;

    public List<JobListingDTO.Response> findAll() {
        return jobListingRepository.findAll().stream()
                .map(JobListingDTO.Response::from)
                .toList();
    }

    public List<JobListingDTO.Response> findActive() {
        return jobListingRepository.findByStatus(JobListing.JobStatus.ACTIVE).stream()
                .map(JobListingDTO.Response::from)
                .toList();
    }

    public JobListingDTO.Response findById(Long id) {
        return jobListingRepository.findById(id)
                .map(JobListingDTO.Response::from)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
    }

    public JobListing findEntityById(Long id) {
        return jobListingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));
    }

    public List<JobListingDTO.Response> findByCompany(Long companyId) {
        return jobListingRepository.findByCompanyId(companyId).stream()
                .map(JobListingDTO.Response::from)
                .toList();
    }

    public List<JobListingDTO.Response> search(String query) {
        return jobListingRepository.search(query).stream()
                .map(JobListingDTO.Response::from)
                .toList();
    }

    public List<JobListingDTO.Response> findRemote() {
        return jobListingRepository.findRemoteJobs().stream()
                .map(JobListingDTO.Response::from)
                .toList();
    }

    public List<String> getAllCategories() {
        return jobListingRepository.findAllCategories();
    }

    public long countActiveJobs() {
        return jobListingRepository.countActiveJobs();
    }

    @Transactional
    public JobListingDTO.Response create(JobListingDTO.Request dto) {
        Company company = companyService.findEntityById(dto.getCompanyId());
        JobListing job = JobListing.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .requirements(dto.getRequirements())
                .salaryMin(dto.getSalaryMin())
                .salaryMax(dto.getSalaryMax())
                .jobType(dto.getJobType())
                .category(dto.getCategory())
                .tags(dto.getTags())
                .remote(dto.isRemote())
                .company(company)
                .build();
        return JobListingDTO.Response.from(jobListingRepository.save(job));
    }

    @Transactional
    public JobListingDTO.Response update(Long id, JobListingDTO.Request dto) {
        JobListing job = findEntityById(id);
        Company company = companyService.findEntityById(dto.getCompanyId());

        job.setTitle(dto.getTitle());
        job.setDescription(dto.getDescription());
        job.setRequirements(dto.getRequirements());
        job.setSalaryMin(dto.getSalaryMin());
        job.setSalaryMax(dto.getSalaryMax());
        job.setJobType(dto.getJobType());
        job.setCategory(dto.getCategory());
        job.setTags(dto.getTags());
        job.setRemote(dto.isRemote());
        job.setCompany(company);

        return JobListingDTO.Response.from(jobListingRepository.save(job));
    }

    @Transactional
    public JobListingDTO.Response updateStatus(Long id, JobListing.JobStatus status) {
        JobListing job = findEntityById(id);
        job.setStatus(status);
        return JobListingDTO.Response.from(jobListingRepository.save(job));
    }

    @Transactional
    public void delete(Long id) {
        if (!jobListingRepository.existsById(id)) {
            throw new RuntimeException("Job not found with id: " + id);
        }
        jobListingRepository.deleteById(id);
    }

    @Transactional
    public JobListing saveFromParser(JobListing job) {
        // Avoid duplicates from external parser
        return jobListingRepository.findByExternalId(job.getExternalId())
                .orElseGet(() -> jobListingRepository.save(job));
    }
}
