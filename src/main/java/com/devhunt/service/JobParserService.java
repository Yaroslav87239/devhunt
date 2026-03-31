package com.devhunt.service;

import com.devhunt.model.Company;
import com.devhunt.model.JobListing;
import com.devhunt.repository.CompanyRepository;
import com.devhunt.repository.JobListingRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * JobParserService — відповідає за збір вакансій із зовнішніх джерел.
 *
 * Джерела:
 *  1. Remotive API (JSON) — https://remotive.com/api/remote-jobs
 *  2. Jsoup-парсер (HTML) — скрапінг сторінок із вакансіями за потреби
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobParserService {

    private final ObjectMapper objectMapper;
    private final CompanyRepository companyRepository;
    private final JobListingRepository jobListingRepository;

    @Value("${app.parser.remotive-url:https://remotive.com/api/remote-jobs}")
    private String remotiveUrl;

    @Value("${app.parser.max-results:20}")
    private int maxResults;

    // ─── Result DTO for reporting ───────────────────────────────────────────

    public record ParseResult(int fetched, int saved, int skipped, String source, String errorMessage) {
        public boolean isSuccess() { return errorMessage == null; }
        public static ParseResult success(int fetched, int saved, int skipped, String source) {
            return new ParseResult(fetched, saved, skipped, source, null);
        }
        public static ParseResult error(String source, String message) {
            return new ParseResult(0, 0, 0, source, message);
        }
    }

    // ─── Scheduled daily sync ───────────────────────────────────────────────

    @Scheduled(cron = "0 0 9 * * *")   // Every day at 09:00
    public void scheduledSync() {
        log.info("⏰ Scheduled job sync starting...");
        ParseResult result = fetchFromRemotiveApi("software-dev");
        log.info("✅ Sync done — fetched={}, saved={}, skipped={}", result.fetched(), result.saved(), result.skipped());
    }

    // ─── 1. Remotive JSON API ───────────────────────────────────────────────

    @Transactional
    public ParseResult fetchFromRemotiveApi(String category) {
        String url = remotiveUrl + "?category=" + category + "&limit=" + maxResults;
        log.info("📡 Fetching from Remotive API: {}", url);

        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "DevHunt/1.0")
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return ParseResult.error("Remotive", "HTTP " + response.statusCode());
            }

            return parseRemotiveJson(response.body());

        } catch (IOException | InterruptedException e) {
            log.warn("⚠️ Remotive API unavailable: {}. Using mock data instead.", e.getMessage());
            return loadMockData();
        }
    }

    private ParseResult parseRemotiveJson(String json) throws IOException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode jobs = root.get("jobs");

        if (jobs == null || !jobs.isArray()) {
            return ParseResult.error("Remotive", "Unexpected JSON structure");
        }

        int saved = 0, skipped = 0;
        List<JobListing> toSave = new ArrayList<>();

        for (JsonNode node : jobs) {
            String externalId = "remotive-" + node.path("id").asText();

            if (jobListingRepository.existsByExternalId(externalId)) {
                skipped++;
                continue;
            }

            // Resolve or create company
            String companyName = node.path("company_name").asText("Unknown Company");
            Company company = companyRepository.findByName(companyName)
                    .orElseGet(() -> companyRepository.save(
                            Company.builder()
                                    .name(companyName)
                                    .logoUrl(node.path("company_logo").asText(null))
                                    .industry("Technology")
                                    .location("Remote")
                                    .build()
                    ));

            // Clean HTML description via Jsoup
            String rawDescription = node.path("description").asText("");
            String cleanDescription = cleanHtml(rawDescription);

            String tagsRaw = "";
            JsonNode tagsNode = node.path("tags");
            if (tagsNode.isArray()) {
                List<String> tagList = new ArrayList<>();
                tagsNode.forEach(t -> tagList.add(t.asText()));
                tagsRaw = String.join(", ", tagList);
            }

            JobListing job = JobListing.builder()
                    .title(node.path("title").asText("Untitled"))
                    .description(cleanDescription)
                    .category(node.path("category").asText("Technology"))
                    .tags(tagsRaw)
                    .remote(true)
                    .jobType(resolveJobType(node.path("job_type").asText("")))
                    .externalId(externalId)
                    .externalUrl(node.path("url").asText(null))
                    .postedAt(LocalDateTime.now())
                    .company(company)
                    .build();

            toSave.add(job);
        }

        jobListingRepository.saveAll(toSave);
        saved = toSave.size();
        log.info("💾 Remotive parse: {} saved, {} skipped", saved, skipped);
        return ParseResult.success(jobs.size(), saved, skipped, "Remotive API");
    }

    // ─── 2. Jsoup HTML Scraper ──────────────────────────────────────────────

    /**
     * Демонстрація Jsoup-парсингу: завантажуємо сторінку вакансії
     * і витягуємо опис у вигляді чистого тексту.
     */
    public String scrapeJobDescriptionFromUrl(String pageUrl) {
        try {
            Document doc = Jsoup.connect(pageUrl)
                    .userAgent("DevHunt/1.0 (+https://devhunt.app)")
                    .timeout(10_000)
                    .get();

            // Try common selectors for job description containers
            String[] selectors = {".job-description", "#job-description", ".description",
                    "[data-testid='job-description']", "article", "main"};

            for (String selector : selectors) {
                var element = doc.selectFirst(selector);
                if (element != null && element.text().length() > 100) {
                    return element.text();
                }
            }
            return doc.body().text();

        } catch (IOException e) {
            log.warn("Jsoup could not fetch URL {}: {}", pageUrl, e.getMessage());
            return "Description not available";
        }
    }

    // ─── Mock data (fallback when API is offline) ───────────────────────────

    @Transactional
    public ParseResult loadMockData() {
        log.info("📦 Loading mock job data...");

        List<String[]> mockCompanies = List.of(
            new String[]{"Stripe", "Fintech", "San Francisco, CA", "https://stripe.com"},
            new String[]{"Vercel", "Developer Tools", "Remote", "https://vercel.com"},
            new String[]{"HashiCorp", "DevOps", "Remote", "https://hashicorp.com"},
            new String[]{"Figma", "Design Tools", "San Francisco, CA", "https://figma.com"}
        );

        record MockJob(String title, String company, String category, String tags,
                       String desc, int salMin, int salMax, String type) {}

        List<MockJob> mockJobs = List.of(
            new MockJob("Senior Backend Engineer", "Stripe", "software-dev",
                "Java, Spring Boot, PostgreSQL, Kafka",
                "Build scalable payment infrastructure serving millions of transactions. You will design high-throughput APIs, maintain PostgreSQL schemas, and contribute to Kafka-based event streaming.",
                130000, 190000, "full_time"),

            new MockJob("Staff Frontend Engineer", "Vercel", "software-dev",
                "React, TypeScript, Next.js, Performance",
                "Shape the future of web development tooling. Lead React performance initiatives and build features used by thousands of developers daily.",
                140000, 200000, "full_time"),

            new MockJob("DevOps / Platform Engineer", "HashiCorp", "devops",
                "Terraform, Kubernetes, AWS, Go",
                "Own infrastructure automation at scale. Build and maintain Terraform modules, Kubernetes operators, and CI/CD pipelines for global deployments.",
                120000, 175000, "full_time"),

            new MockJob("Product Designer (B2B)", "Figma", "design",
                "UX, Figma, Design Systems, B2B",
                "Define design patterns for enterprise tools. Work closely with engineering to build cohesive design systems used across product lines.",
                110000, 160000, "full_time"),

            new MockJob("Junior Java Developer", "Stripe", "software-dev",
                "Java, Spring, REST, SQL",
                "Join the Payments team and help build features that handle billions in transactions. Great mentorship culture.",
                70000, 100000, "full_time"),

            new MockJob("Freelance React Developer", "Vercel", "software-dev",
                "React, Next.js, TypeScript",
                "Short-term contract to help migrate legacy pages to Next.js App Router. Fully remote, flexible hours.",
                0, 0, "contract")
        );

        int saved = 0, skipped = 0;

        for (MockJob mj : mockJobs) {
            String externalId = "mock-" + mj.title().toLowerCase().replaceAll("\\s+", "-")
                                + "-" + mj.company().toLowerCase();

            if (jobListingRepository.existsByExternalId(externalId)) {
                skipped++;
                continue;
            }

            String[] cd = mockCompanies.stream()
                    .filter(c -> c[0].equals(mj.company())).findFirst()
                    .orElse(new String[]{mj.company(), "Technology", "Remote", ""});

            Company company = companyRepository.findByName(cd[0])
                    .orElseGet(() -> companyRepository.save(
                            Company.builder()
                                    .name(cd[0]).industry(cd[1])
                                    .location(cd[2]).website(cd[3])
                                    .build()
                    ));

            Integer salMin = mj.salMin() == 0 ? null : mj.salMin();
            Integer salMax = mj.salMax() == 0 ? null : mj.salMax();

            JobListing job = JobListing.builder()
                    .title(mj.title())
                    .description(mj.desc())
                    .category(mj.category())
                    .tags(mj.tags())
                    .remote(true)
                    .salaryMin(salMin)
                    .salaryMax(salMax)
                    .jobType(resolveJobType(mj.type()))
                    .externalId(externalId)
                    .postedAt(LocalDateTime.now())
                    .company(company)
                    .build();

            jobListingRepository.save(job);
            saved++;
        }

        return ParseResult.success(mockJobs.size(), saved, skipped, "Mock Data");
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    private String cleanHtml(String html) {
        if (html == null || html.isBlank()) return "";
        return Jsoup.parse(html).text();
    }

    private JobListing.JobType resolveJobType(String raw) {
        if (raw == null) return JobListing.JobType.FULL_TIME;
        return switch (raw.toLowerCase().replace(" ", "_")) {
            case "part_time", "part-time"   -> JobListing.JobType.PART_TIME;
            case "contract"                 -> JobListing.JobType.CONTRACT;
            case "freelance"                -> JobListing.JobType.FREELANCE;
            case "internship", "intern"     -> JobListing.JobType.INTERNSHIP;
            default                         -> JobListing.JobType.FULL_TIME;
        };
    }
}
