package ru.practicum.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dto.PaymentRequestDto;
import ru.practicum.dto.RefundRequestDto;
import ru.practicum.dto.UserBalanceResponseDto;
import ru.practicum.repository.balance.UserBalanceRepository;
import ru.practicum.repository.transaction.TransactionRepository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.UUID;

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class PaymentControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    protected WebTestClient getWebTestClientWithMockUser() {
        Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_payment.read", "SCOPE_payment.write");
        return webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockJwt().authorities(authorities))
                .mutateWith(SecurityMockServerConfigurers.csrf());
    }

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private final UUID testUserId = UUID.randomUUID();
    private final UUID testOrderId = UUID.randomUUID();
    private final BigDecimal testAmount = BigDecimal.valueOf(100.50);
    private final BigDecimal initialBalance = BigDecimal.valueOf(500.00);

    @BeforeEach
    void setUp() {
        clearData().block();
        userBalanceRepository.create(testUserId, initialBalance).block();
    }

    @AfterEach
    void tearDown() {
        clearData().block();
    }

    private Mono<Void> clearData() {
        return transactionRepository.deleteAll()
                .then(userBalanceRepository.deleteAll());
    }

    @Test
    void getBalance_shouldReturnUserBalance() {
        getWebTestClientWithMockUser().get()
                .uri("/payment/{userUuid}/balance", testUserId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserBalanceResponseDto.class)
                .value(dto -> {
                    assert dto.getUserUuid().equals(testUserId);
                    assert dto.getBalance().compareTo(initialBalance) == 0;
                });
    }

    @Test
    void processPayment_shouldProcessSuccessfulPayment() {
        PaymentRequestDto request = new PaymentRequestDto(testUserId, testAmount, testOrderId);

        getWebTestClientWithMockUser().post()
                .uri("/payment")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userUuid").isEqualTo(testUserId.toString())
                .jsonPath("$.isSuccess").isEqualTo(true)
                .jsonPath("$.transactionUuid").hasJsonPath()
                .jsonPath("$.newBalance").hasJsonPath();

        StepVerifier.create(transactionRepository.count())
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void processPayment_shouldHandleInsufficientFunds() {
        PaymentRequestDto request = new PaymentRequestDto(testUserId, initialBalance.add(BigDecimal.ONE), testOrderId);

        getWebTestClientWithMockUser().post()
                .uri("/payment")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userUuid").isEqualTo(testUserId.toString())
                .jsonPath("$.isSuccess").isEqualTo(false)
                .jsonPath("$.transactionUuid").hasJsonPath()
                .jsonPath("$.newBalance").hasJsonPath();

        StepVerifier.create(transactionRepository.count())
                .expectNext(1L)
                .verifyComplete();
    }

    @Test
    void processRefund_shouldProcessSuccessfulRefund() {
        PaymentRequestDto paymentRequest = new PaymentRequestDto(testUserId, testAmount, testOrderId);
        getWebTestClientWithMockUser().post()
                .uri("/payment")
                .bodyValue(paymentRequest)
                .exchange();

        RefundRequestDto refundRequest = new RefundRequestDto(testUserId, testAmount, testOrderId);

        getWebTestClientWithMockUser().post()
                .uri("/payment/refund")
                .bodyValue(refundRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userUuid").isEqualTo(testUserId.toString())
                .jsonPath("$.isSuccess").isEqualTo(true)
                .jsonPath("$.transactionUuid").hasJsonPath()
                .jsonPath("$.newBalance").isEqualTo(initialBalance.doubleValue())
                .jsonPath("$.message").hasJsonPath();

        StepVerifier.create(transactionRepository.count())
                .expectNext(2L)
                .verifyComplete();
    }

    @Test
    void processPayment_whenAmountNegative_shouldValidateRequest() {
        PaymentRequestDto invalidRequest = new PaymentRequestDto(testUserId, BigDecimal.valueOf(-100), testOrderId);

        getWebTestClientWithMockUser().post()
                .uri("/payment")
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").hasJsonPath()
                .jsonPath("$.message").hasJsonPath()
                .jsonPath("$.fieldErrors").hasJsonPath();
    }

    @Test
    void processPayment_whenUserUuidNull_shouldValidateRequest() {
        PaymentRequestDto nullUserIdRequest = new PaymentRequestDto(null, testAmount, testOrderId);

        getWebTestClientWithMockUser().post()
                .uri("/payment")
                .bodyValue(nullUserIdRequest)
                .exchange()
                .expectBody()
                .jsonPath("$.status").hasJsonPath()
                .jsonPath("$.message").hasJsonPath()
                .jsonPath("$.fieldErrors").hasJsonPath();
    }

}