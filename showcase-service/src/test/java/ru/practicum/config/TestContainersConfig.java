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
                .waitingFor(Wait.forListeningPort())
                .withReuse(true);

        redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.0-alpine"))
                .withExposedPorts(6379)
                .waitingFor(Wait.forListeningPort())
                .withReuse(true);

        postgresContainer.start();
        redisContainer.start();
    }
}