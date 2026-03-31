package com.devhunt.controller;

import com.devhunt.service.JobParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/parser")
@RequiredArgsConstructor
public class ParserRestController {

    private final JobParserService jobParserService;

    /**
     * POST /api/parser/sync?category=software-dev
     * Manually trigger a fetch from Remotive API for a given category.
     */
    @PostMapping("/sync")
    public ResponseEntity<Map<String, Object>> syncFromRemotive(
            @RequestParam(defaultValue = "software-dev") String category) {

        JobParserService.ParseResult result = jobParserService.fetchFromRemotiveApi(category);
        return ResponseEntity.ok(Map.of(
                "source",  result.source(),
                "fetched", result.fetched(),
                "saved",   result.saved(),
                "skipped", result.skipped(),
                "success", result.isSuccess(),
                "error",   result.errorMessage() != null ? result.errorMessage() : ""
        ));
    }

    /**
     * POST /api/parser/mock
     * Load built-in mock data (useful for demo / offline mode).
     */
    @PostMapping("/mock")
    public ResponseEntity<Map<String, Object>> loadMockData() {
        JobParserService.ParseResult result = jobParserService.loadMockData();
        return ResponseEntity.ok(Map.of(
                "source",  result.source(),
                "fetched", result.fetched(),
                "saved",   result.saved(),
                "skipped", result.skipped(),
                "success", result.isSuccess()
        ));
    }

    /**
     * GET /api/parser/scrape?url=https://...
     * Scrape a job description page with Jsoup and return plain text.
     */
    @GetMapping("/scrape")
    public ResponseEntity<Map<String, String>> scrapeUrl(@RequestParam String url) {
        String text = jobParserService.scrapeJobDescriptionFromUrl(url);
        return ResponseEntity.ok(Map.of("url", url, "content", text));
    }
}
