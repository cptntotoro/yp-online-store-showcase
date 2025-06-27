package ru.practicum.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ValidationDepth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.reactive.TransactionalOperator;

import java.time.Duration;

@Configuration
@Profile("prod")
@EnableTransactionManagement
@SpringBootApplication(scanBasePackages = "ru.practicum")
@EnableR2dbcRepositories(basePackages = "ru.practicum.repository")
public class R2dbcConfiguration {

    @Value("${spring.r2dbc.url}")
    private String url;

    @Value("${spring.r2dbc.username}")
    private String username;

    @Value("${spring.r2dbc.password}")
    private String password;

    @Value("${spring.r2dbc.pool.validation-query:SELECT 1}")
    private String validationQuery;

    @Value("${spring.r2dbc.pool.initial-size:5}")
    private int initialSize;

    @Value("${spring.r2dbc.pool.max-size:20}")
    private int maxSize;

    @Value("${spring.r2dbc.pool.max-idle-time:30m}")
    private Duration maxIdleTime;

    @Value("${spring.r2dbc.pool.max-life-time:60m}")
    private Duration maxLifeTime;

    @Bean
    public ConnectionFactory connectionFactory() {
        return ConnectionFactoryBuilder.withUrl(url)
                .username(username)
                .password(password)
                .build();
    }

    @Bean
    @Primary
    public ConnectionPool connectionPool(ConnectionFactory connectionFactory) {
        ConnectionPoolConfiguration configuration = ConnectionPoolConfiguration.builder(connectionFactory)
                .initialSize(initialSize)
                .maxSize(maxSize)
                .maxIdleTime(maxIdleTime)
                .maxLifeTime(maxLifeTime)
                .maxAcquireTime(Duration.ofSeconds(30))
                .maxCreateConnectionTime(Duration.ofSeconds(30))
                .validationQuery(validationQuery)
                .registerJmx(false)
                .acquireRetry(3)
                .validationDepth(ValidationDepth.LOCAL)
                .maxValidationTime(Duration.ofSeconds(5))
                .backgroundEvictionInterval(Duration.ofMinutes(5))
                .build();

        return new ConnectionPool(configuration);
    }

    @Bean
    @Primary
    public DatabaseClient databaseClient(ConnectionPool connectionPool) {
        return DatabaseClient.create(connectionPool);
    }

    @Bean
    @Primary
    public ReactiveTransactionManager transactionManager(ConnectionPool connectionPool) {
        R2dbcTransactionManager txManager = new R2dbcTransactionManager(connectionPool);
        txManager.setEnforceReadOnly(true);
        return txManager;
    }

    @Bean
    @Primary
    public TransactionalOperator transactionalOperator(ReactiveTransactionManager transactionManager) {
        return TransactionalOperator.create(transactionManager);
    }

    @Bean
    @Primary
    public ConnectionFactoryInitializer databaseInitializer(ConnectionFactory connectionFactory) {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setSqlScriptEncoding("UTF-8");
        populator.setIgnoreFailedDrops(true);
        populator.setContinueOnError(true);

        populator.addScript(new ClassPathResource("schema.sql"));
        populator.addScript(new ClassPathResource("data.sql"));
        populator.setSeparator(";");

        initializer.setDatabasePopulator(populator);
        return initializer;
    }
}