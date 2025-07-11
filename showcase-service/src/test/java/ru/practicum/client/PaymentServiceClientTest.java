package ru.practicum.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.client.api.PaymentApi;
import ru.practicum.client.dto.PaymentRequestDto;
import ru.practicum.client.dto.PaymentResponseDto;
import ru.practicum.client.dto.RefundRequestDto;
import ru.practicum.client.dto.RefundResponseDto;
import ru.practicum.client.dto.UserBalanceResponseDto;
import ru.practicum.exception.payment.PaymentProcessingException;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.balance.UserBalance;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceClientTest {

    @Mock
    private WebClient webClient;

    @Mock
    private PaymentApi paymentApiClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private PaymentServiceClientImpl paymentServiceClient;

    private final UUID testUserId = UUID.randomUUID();
    private final UUID testOrderId = UUID.randomUUID();
    private final BigDecimal testAmount = BigDecimal.valueOf(100.50);

    @Test
    void processPayment_shouldSuccessfullyProcessPayment() {
        PaymentResponseDto responseDto = new PaymentResponseDto()
                .userUuid(testUserId)
                .isSuccess(true)
                .transactionUuid(UUID.randomUUID())
                .newBalance(BigDecimal.valueOf(500.00));

        when(paymentApiClient.processPayment(any(PaymentRequestDto.class)))
                .thenReturn(Mono.just(responseDto));

        StepVerifier.create(paymentServiceClient.processPayment(testUserId, testOrderId, testAmount))
                .verifyComplete();
    }

    @Test
    void processPayment_shouldThrowWhenPaymentFails() {
        PaymentResponseDto responseDto = new PaymentResponseDto()
                .userUuid(testUserId)
                .isSuccess(false)
                .transactionUuid(UUID.randomUUID())
                .newBalance(BigDecimal.valueOf(50.00));

        when(paymentApiClient.processPayment(any(PaymentRequestDto.class)))
                .thenReturn(Mono.just(responseDto));

        StepVerifier.create(paymentServiceClient.processPayment(testUserId, testOrderId, testAmount))
                .expectErrorMatches(e -> e instanceof PaymentProcessingException &&
                        e.getMessage().contains("Недостаточно средств на счете"))
                .verify();
    }

    @Test
    void processPayment_shouldThrowWhenServiceUnavailable() {
        WebClientRequestException exception = new WebClientRequestException(
                new RuntimeException("Connection refused"),
                HttpMethod.POST,
                URI.create("http://payment-service/payment"),
                new HttpHeaders()
        );

        when(paymentApiClient.processPayment(any(PaymentRequestDto.class)))
                .thenReturn(Mono.error(exception));

        StepVerifier.create(paymentServiceClient.processPayment(testUserId, testOrderId, testAmount))
                .expectErrorMatches(e -> {
                    if (!(e instanceof PaymentProcessingException)) {
                        return false;
                    }
                    return e.getMessage().contains("Сервис платежей недоступен") ||
                            e.getMessage().contains("Ошибка при обработке платежа");
                })
                .verify();
    }

    @Test
    void processRefund_shouldSuccessfullyProcessRefund() {
        RefundResponseDto responseDto = new RefundResponseDto()
                .userUuid(testUserId)
                .isSuccess(true)
                .transactionUuid(UUID.randomUUID())
                .newBalance(BigDecimal.valueOf(600.00))
                .message("Refund processed successfully");

        when(paymentApiClient.processRefund(any(RefundRequestDto.class)))
                .thenReturn(Mono.just(responseDto));

        StepVerifier.create(paymentServiceClient.processRefund(testUserId, testOrderId, testAmount))
                .verifyComplete();
    }

    @Test
    void processRefund_shouldThrowWhenRefundFails() {
        RefundResponseDto responseDto = new RefundResponseDto()
                .userUuid(testUserId)
                .isSuccess(false)
                .transactionUuid(UUID.randomUUID())
                .newBalance(BigDecimal.valueOf(500.00))
                .message("Insufficient funds for refund");

        when(paymentApiClient.processRefund(any(RefundRequestDto.class)))
                .thenReturn(Mono.just(responseDto));

        StepVerifier.create(paymentServiceClient.processRefund(testUserId, testOrderId, testAmount))
                .expectErrorMatches(e -> e instanceof PaymentProcessingException &&
                        e.getMessage().contains("Insufficient funds for refund"))
                .verify();
    }

    @Test
    void getBalance_shouldReturnUserBalance() {
        UserBalanceResponseDto responseDto = new UserBalanceResponseDto()
                .userUuid(testUserId)
                .balance(BigDecimal.valueOf(1000.00));

        UserBalance expectedBalance = new UserBalance(
                testUserId,
                BigDecimal.valueOf(1000.00)
        );

        when(paymentApiClient.getBalance(testUserId))
                .thenReturn(Mono.just(responseDto));
        when(userMapper.userBalanceResponseDtoToUserBalance(responseDto))
                .thenReturn(expectedBalance);

        StepVerifier.create(paymentServiceClient.getBalance(testUserId))
                .expectNext(expectedBalance)
                .verifyComplete();
    }

    @Test
    void checkHealth_shouldReturnTrueWhenServiceIsUp() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/actuator/health")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(Map.of("status", "UP")));

        StepVerifier.create(paymentServiceClient.checkHealth())
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void checkHealth_shouldReturnFalseWhenServiceIsDown() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/actuator/health")).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.error(new WebClientResponseException(503, "Service Unavailable", null, null, null)));

        StepVerifier.create(paymentServiceClient.checkHealth())
                .expectNext(false)
                .verifyComplete();
    }
}