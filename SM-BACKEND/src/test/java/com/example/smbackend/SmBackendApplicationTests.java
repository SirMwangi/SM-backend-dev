package com.example.smbackend;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring application context loads successfully.
 *
 * <p>Uses the {@code test} profile so the H2 in-memory datasource defined in
 * {@code src/test/resources/application.yml} is used instead of Postgres.
 */
@SpringBootTest
@ActiveProfiles("test")
class SmBackendApplicationTests {

    @Test
    @DisplayName("Spring application context should load without errors")
    void contextLoads() {
        // If the context fails to start, Spring Boot will throw before this
        // method is reached and the test will fail with a descriptive exception.
    }
}
