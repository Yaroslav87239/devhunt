package com.devhunt.controller;

import com.devhunt.dto.CompanyDTO;
import com.devhunt.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
public class CompanyRestController {

    private final CompanyService companyService;

    @GetMapping
    public ResponseEntity<List<CompanyDTO.Response>> getAll(
            @RequestParam(required = false) String search) {
        List<CompanyDTO.Response> result = search != null && !search.isBlank()
                ? companyService.search(search)
                : companyService.findAll();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.findById(id));
    }

    @PostMapping
    public ResponseEntity<CompanyDTO.Response> create(@Valid @RequestBody CompanyDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyDTO.Response> update(
            @PathVariable Long id,
            @Valid @RequestBody CompanyDTO.Request dto) {
        return ResponseEntity.ok(companyService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.ok(Map.of("message", "Company deleted successfully"));
    }

    @GetMapping("/industries")
    public ResponseEntity<List<String>> getIndustries() {
        return ResponseEntity.ok(companyService.getAllIndustries());
    }
}
