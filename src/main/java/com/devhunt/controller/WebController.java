package com.devhunt.controller;

import com.devhunt.dto.ApplicationDTO;
import com.devhunt.model.JobListing;
import com.devhunt.service.ApplicationService;
import com.devhunt.service.CompanyService;
import com.devhunt.service.JobListingService;
import com.devhunt.service.JobParserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class WebController {

    private final JobListingService jobListingService;
    private final CompanyService companyService;
    private final ApplicationService applicationService;
    private final JobParserService jobParserService;

    // ─── Home ────────────────────────────────────────────────────────────────

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("activeJobs",    jobListingService.findActive());
        model.addAttribute("companies",     companyService.findAll());
        model.addAttribute("categories",    jobListingService.getAllCategories());
        model.addAttribute("activeCount",   jobListingService.countActiveJobs());
        model.addAttribute("pendingApps",   applicationService.countPending());
        return "index";
    }

    // ─── Jobs ─────────────────────────────────────────────────────────────────

    @GetMapping("/jobs")
    public String jobs(Model model,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) Boolean remote) {

        var jobs = (search != null && !search.isBlank())
                ? jobListingService.search(search)
                : (Boolean.TRUE.equals(remote))
                    ? jobListingService.findRemote()
                    : jobListingService.findActive();

        if (category != null && !category.isBlank()) {
            String cat = category.toLowerCase();
            jobs = jobs.stream()
                    .filter(j -> j.getCategory() != null && j.getCategory().toLowerCase().contains(cat))
                    .toList();
        }

        model.addAttribute("jobs",       jobs);
        model.addAttribute("categories", jobListingService.getAllCategories());
        model.addAttribute("search",     search);
        model.addAttribute("category",   category);
        model.addAttribute("remote",     remote);
        return "jobs";
    }

    @GetMapping("/jobs/{id}")
    public String jobDetail(@PathVariable Long id, Model model) {
        model.addAttribute("job", jobListingService.findById(id));
        model.addAttribute("applications", applicationService.findByJob(id));

        ApplicationDTO.Request form = new ApplicationDTO.Request();
        form.setJobListingId(id);
        model.addAttribute("applyForm", form);

        return "job-detail";
    }

    // ─── Apply ────────────────────────────────────────────────────────────────

    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable Long id,
                        @Valid @ModelAttribute("applyForm") ApplicationDTO.Request form,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("job",          jobListingService.findById(id));
            model.addAttribute("applications", applicationService.findByJob(id));
            return "job-detail";
        }

        try {
            form.setJobListingId(id);
            applicationService.apply(form);
            redirectAttributes.addFlashAttribute("successMsg",
                    "✅ Application submitted! We'll be in touch soon.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMsg", "⚠️ " + e.getMessage());
        }
        return "redirect:/jobs/" + id;
    }

    // ─── Companies ────────────────────────────────────────────────────────────

    @GetMapping("/companies")
    public String companies(Model model,
                            @RequestParam(required = false) String search) {
        var list = (search != null && !search.isBlank())
                ? companyService.search(search)
                : companyService.findAll();
        model.addAttribute("companies",  list);
        model.addAttribute("industries", companyService.getAllIndustries());
        model.addAttribute("search",     search);
        return "companies";
    }

    // ─── Admin: Applications dashboard ───────────────────────────────────────

    @GetMapping("/admin/applications")
    public String adminApplications(Model model,
                                     @RequestParam(required = false) Long jobId) {
        var apps = (jobId != null)
                ? applicationService.findByJob(jobId)
                : applicationService.findAll();
        model.addAttribute("applications", apps);
        model.addAttribute("jobs",         jobListingService.findActive());
        model.addAttribute("selectedJobId",jobId);
        return "admin/applications";
    }

    @PostMapping("/admin/applications/{id}/status")
    public String updateAppStatus(@PathVariable Long id,
                                  @RequestParam String status,
                                  RedirectAttributes redirectAttributes) {
        try {
            applicationService.updateStatus(id,
                    com.devhunt.model.JobApplication.ApplicationStatus.valueOf(status));
            redirectAttributes.addFlashAttribute("successMsg", "Status updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/applications";
    }

    // ─── Admin: Parser trigger ────────────────────────────────────────────────

    @PostMapping("/admin/parser/sync")
    public String syncJobs(@RequestParam(defaultValue = "software-dev") String category,
                           RedirectAttributes ra) {
        var result = jobParserService.fetchFromRemotiveApi(category);
        if (result.isSuccess()) {
            ra.addFlashAttribute("successMsg",
                    "✅ Sync complete — saved " + result.saved() + " new jobs from " + result.source());
        } else {
            ra.addFlashAttribute("errorMsg", "❌ Parse error: " + result.errorMessage());
        }
        return "redirect:/";
    }

    // ─── Job status toggle (admin) ────────────────────────────────────────────

    @PostMapping("/admin/jobs/{id}/status")
    public String toggleJobStatus(@PathVariable Long id,
                                  @RequestParam JobListing.JobStatus status,
                                  RedirectAttributes ra) {
        jobListingService.updateStatus(id, status);
        ra.addFlashAttribute("successMsg", "Job status updated to " + status);
        return "redirect:/jobs/" + id;
    }
}
