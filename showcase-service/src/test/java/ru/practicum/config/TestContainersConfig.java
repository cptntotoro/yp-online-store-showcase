package ru.practicum.config;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

public class TestContainersConfig {
    public static final PostgreSQLContainer<?> postgresContainer;
    public static final GenericContainer<?> redisContainer;

    static {
        postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("test_db")
                .withUsername("postgres")
                .withPassword("postgres")
                .withInitScript("test-schema.sql")
                .waitingFor(Wait.forListeningPort());

        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
                .withExposedPorts(6379)
                .waitingFor(Wait.forListeningPort());

        postgresContainer.start();
        redisContainer.start();

        // Добавляем задержку для инициализации
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}