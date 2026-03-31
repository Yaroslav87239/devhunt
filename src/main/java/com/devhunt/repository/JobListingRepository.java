package com.devhunt.repository;

import com.devhunt.model.JobListing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobListingRepository extends JpaRepository<JobListing, Long> {

    List<JobListing> findByStatus(JobListing.JobStatus status);

    List<JobListing> findByCompanyId(Long companyId);

    List<JobListing> findByCompanyIdAndStatus(Long companyId, JobListing.JobStatus status);

    Optional<JobListing> findByExternalId(String externalId);

    boolean existsByExternalId(String externalId);

    List<JobListing> findByCategoryIgnoreCase(String category);

    @Query("SELECT j FROM JobListing j WHERE j.status = 'ACTIVE' AND (" +
           "LOWER(j.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.category) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.tags) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(j.company.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<JobListing> search(String query);

    @Query("SELECT j FROM JobListing j WHERE j.status = 'ACTIVE' AND j.remote = true")
    List<JobListing> findRemoteJobs();

    @Query("SELECT DISTINCT j.category FROM JobListing j ORDER BY j.category")
    List<String> findAllCategories();

    @Query("SELECT COUNT(j) FROM JobListing j WHERE j.status = 'ACTIVE'")
    long countActiveJobs();
}
