package com.devhunt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DevHuntApplication {

    public static void main(String[] args) {
        SpringApplication.run(DevHuntApplication.class, args);
        System.out.println("""
                ╔══════════════════════════════════════╗
                ║   🚀 DevHunt — Jobs Aggregator       ║
                ║   http://localhost:8080               ║
                ║   H2 Console: /h2-console             ║
                ╚══════════════════════════════════════╝
                """);
    }
}
