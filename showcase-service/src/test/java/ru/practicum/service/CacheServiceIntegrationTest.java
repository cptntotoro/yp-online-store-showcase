package ru.practicum.service;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practicum.config.TestContainersConfig;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public abstract class CacheServiceIntegrationTest {

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                String.format("r2dbc:postgresql://%s:%d/%s",
                        TestContainersConfig.postgresContainer.getHost(),
                        TestContainersConfig.postgresContainer.getFirstMappedPort(),
                        TestContainersConfig.postgresContainer.getDatabaseName()));

        registry.add("spring.r2dbc.username", TestContainersConfig.postgresContainer::getUsername);
        registry.add("spring.r2dbc.password", TestContainersConfig.postgresContainer::getPassword);

        registry.add("spring.data.redis.host", TestContainersConfig.redisContainer::getHost);
        registry.add("spring.data.redis.port", TestContainersConfig.redisContainer::getFirstMappedPort);
    }
}
