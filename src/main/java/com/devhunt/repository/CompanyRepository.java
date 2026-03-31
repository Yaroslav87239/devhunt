package com.devhunt.repository;

import com.devhunt.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    boolean existsByName(String name);

    List<Company> findByIndustryIgnoreCase(String industry);

    @Query("SELECT c FROM Company c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(c.industry) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Company> search(String query);

    @Query("SELECT DISTINCT c.industry FROM Company c ORDER BY c.industry")
    List<String> findAllIndustries();
}
