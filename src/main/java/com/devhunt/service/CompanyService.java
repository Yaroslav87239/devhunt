package com.devhunt.service;

import com.devhunt.dto.CompanyDTO;
import com.devhunt.model.Company;
import com.devhunt.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    public List<CompanyDTO.Response> findAll() {
        return companyRepository.findAll().stream()
                .map(CompanyDTO.Response::from)
                .toList();
    }

    public CompanyDTO.Response findById(Long id) {
        return companyRepository.findById(id)
                .map(CompanyDTO.Response::from)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
    }

    public Company findEntityById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
    }

    @Transactional
    public CompanyDTO.Response create(CompanyDTO.Request dto) {
        if (companyRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Company with name '" + dto.getName() + "' already exists");
        }
        Company company = Company.builder()
                .name(dto.getName())
                .website(dto.getWebsite())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .logoUrl(dto.getLogoUrl())
                .industry(dto.getIndustry())
                .build();
        return CompanyDTO.Response.from(companyRepository.save(company));
    }

    @Transactional
    public CompanyDTO.Response update(Long id, CompanyDTO.Request dto) {
        Company company = findEntityById(id);
        company.setName(dto.getName());
        company.setWebsite(dto.getWebsite());
        company.setDescription(dto.getDescription());
        company.setLocation(dto.getLocation());
        company.setLogoUrl(dto.getLogoUrl());
        company.setIndustry(dto.getIndustry());
        return CompanyDTO.Response.from(companyRepository.save(company));
    }

    @Transactional
    public void delete(Long id) {
        if (!companyRepository.existsById(id)) {
            throw new RuntimeException("Company not found with id: " + id);
        }
        companyRepository.deleteById(id);
    }

    public List<CompanyDTO.Response> search(String query) {
        return companyRepository.search(query).stream()
                .map(CompanyDTO.Response::from)
                .toList();
    }

    public List<String> getAllIndustries() {
        return companyRepository.findAllIndustries();
    }
}
