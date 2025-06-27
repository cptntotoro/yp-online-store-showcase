package ru.practicum.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.config.TestR2dbcConfiguration;
import ru.practicum.dao.balance.UserBalanceDao;
import ru.practicum.repository.balance.UserBalanceRepository;
import ru.practicum.repository.transaction.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@DataR2dbcTest
@Import(TestR2dbcConfiguration.class)
class UserBalanceRepositoryTest {

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private DatabaseClient databaseClient;

    private UUID testUserId;

    @BeforeEach
    void setupDatabase() {
        // Очищаем таблицы перед каждым тестом
        transactionRepository.deleteAll().block();
        userBalanceRepository.deleteAll().block();

        databaseClient.sql("DELETE FROM users").fetch().rowsUpdated().block();

        // Создаем тестового пользователя напрямую в БД
        testUserId = UUID.randomUUID();
        databaseClient.sql("INSERT INTO users (user_uuid, username, email) VALUES (:userId, 'test_user', 'test@example.com')")
                .bind("userId", testUserId)
                .fetch()
                .rowsUpdated()
                .block();
    }

    private Mono<UserBalanceDao> createTestBalance(BigDecimal amount) {
        return databaseClient.sql(
                        "INSERT INTO user_balances (user_uuid, amount, created_at, updated_at) " +
                                "VALUES (:userId, :amount, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) " +
                                "RETURNING *")
                .bind("userId", testUserId)
                .bind("amount", amount)
                .map((row, metadata) -> {
                    UserBalanceDao balance = new UserBalanceDao();
                    balance.setUuid(row.get("balance_uuid", UUID.class));
                    balance.setUserUuid(row.get("user_uuid", UUID.class));
                    balance.setAmount(row.get("amount", BigDecimal.class));
                    balance.setCreatedAt(row.get("created_at", LocalDateTime.class));
                    balance.setUpdatedAt(row.get("updated_at", LocalDateTime.class));
                    return balance;
                })
                .one();
    }

    @Test
    void findByUserUuid_shouldReturnBalanceWhenExists() {
        BigDecimal testAmount = new BigDecimal("1000.00");

        createTestBalance(testAmount)
                .thenMany(userBalanceRepository.findByUserUuid(testUserId))
                .as(StepVerifier::create)
                .expectNextMatches(balance ->
                        balance.getAmount().compareTo(testAmount) == 0 &&
                                balance.getUserUuid().equals(testUserId))
                .verifyComplete();
    }

    @Test
    void findByUserUuid_shouldReturnEmptyWhenNotExists() {
        userBalanceRepository.findByUserUuid(testUserId)
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void deductBalance_shouldDeductAmountWhenSufficientFunds() {
        BigDecimal initialAmount = new BigDecimal("1000.00");
        BigDecimal deductAmount = new BigDecimal("300.00");

        createTestBalance(initialAmount)
                .then(userBalanceRepository.deductBalance(testUserId, deductAmount))
                .as(StepVerifier::create)
                .expectNextMatches(updatedBalance ->
                        updatedBalance.getAmount().compareTo(initialAmount.subtract(deductAmount)) == 0)
                .verifyComplete();
    }

    @Test
    void deductBalance_shouldNotDeductWhenInsufficientFunds() {
        BigDecimal initialAmount = new BigDecimal("100.00");
        BigDecimal deductAmount = new BigDecimal("300.00");

        createTestBalance(initialAmount)
                .then(userBalanceRepository.deductBalance(testUserId, deductAmount))
                .as(StepVerifier::create)
                .verifyComplete();
    }

    @Test
    void addToBalance_shouldIncreaseBalance() {
        BigDecimal initialAmount = new BigDecimal("500.00");
        BigDecimal addAmount = new BigDecimal("300.00");

        createTestBalance(initialAmount)
                .then(userBalanceRepository.addToBalance(testUserId, addAmount))
                .as(StepVerifier::create)
                .expectNextMatches(updatedBalance ->
                        updatedBalance.getAmount().compareTo(initialAmount.add(addAmount)) == 0)
                .verifyComplete();
    }
}