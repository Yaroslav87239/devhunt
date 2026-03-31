package com.devhunt.repository;

import com.devhunt.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByJobListingId(Long jobListingId);

    List<JobApplication> findByEmailIgnoreCase(String email);

    List<JobApplication> findByStatus(JobApplication.ApplicationStatus status);

    boolean existsByEmailAndJobListingId(String email, Long jobListingId);

    @Query("SELECT COUNT(a) FROM JobApplication a WHERE a.status = 'PENDING'")
    long countPending();

    @Query("SELECT a FROM JobApplication a WHERE a.jobListing.company.id = :companyId")
    List<JobApplication> findByCompanyId(Long companyId);
}
