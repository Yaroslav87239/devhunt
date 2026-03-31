package com.devhunt.config;

import com.devhunt.service.JobParserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

    private final JobParserService jobParserService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("🌱 Seeding initial data...");
        // First try live API; falls back to mock data automatically if offline
        JobParserService.ParseResult result = jobParserService.fetchFromRemotiveApi("software-dev");
        log.info("🌱 Seed complete — source={}, saved={}, skipped={}",
                result.source(), result.saved(), result.skipped());
    }
}
