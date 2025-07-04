package ru.practicum.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import ru.practicum.client.dto.balance.UserBalanceResponseDto;
import ru.practicum.client.dto.payment.PaymentRequestDto;
import ru.practicum.client.dto.payment.PaymentResponseDto;
import ru.practicum.client.dto.payment.RefundResponseDto;
import ru.practicum.exception.payment.PaymentProcessingException;
import ru.practicum.exception.payment.PaymentServiceUnavailableException;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.balance.UserBalance;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

/**
 * Клиент сервиса оплаты
 */
@Component
@RequiredArgsConstructor
public class PaymentServiceClientImpl implements PaymentServiceClient {

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    private final WebClient webClient;
    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    @Override
    public Mono<Void> processPayment(UUID userUuid, UUID orderUuid, BigDecimal total) {
        PaymentRequestDto request = PaymentRequestDto.builder()
                .userUuid(userUuid)
                .orderUuid(orderUuid)
                .amount(total)
                .build();

        return webClient.post()
                .uri("/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new PaymentProcessingException(
                                        "Ошибка при обработке платежа: " + error))))
                .bodyToMono(PaymentResponseDto.class)
                .flatMap(response -> {
                    if (!response.isSuccess()) {
                        return Mono.error(new PaymentProcessingException(
                                "Недостаточно средств на счете. TransactionUuid: " +
                                        response.getTransactionUuid()));
                    }
                    return Mono.<Void>empty();
                })
                .timeout(TIMEOUT)
                .onErrorMap(WebClientRequestException.class, e ->
                        new PaymentServiceUnavailableException("Сервис платежей недоступен"));
    }

    @Override
    public Mono<Void> processRefund(UUID userUuid, UUID orderUuid, BigDecimal total) {
        PaymentRequestDto request = PaymentRequestDto.builder()
                .userUuid(userUuid)
                .orderUuid(orderUuid)
                .amount(total)
                .build();

        return webClient.post()
                .uri("/payment/refund")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new PaymentProcessingException(
                                        "Ошибка при возврате средств: " + error))
                                )
                )
                .bodyToMono(RefundResponseDto.class)
                .flatMap(response -> {
                    if (!response.isSuccess()) {
                        return Mono.error(new PaymentProcessingException(
                                response.getMessage() != null ? response.getMessage() :
                                        "Ошибка при возврате средств. TransactionUuid: " + response.getTransactionUuid()));
                    }
                    return Mono.<Void>empty();
                })
                .timeout(TIMEOUT)
                .onErrorMap(WebClientRequestException.class, e ->
                        new PaymentServiceUnavailableException("Сервис платежей недоступен"));
    }

    @Override
    public Mono<UserBalance> getBalance(UUID userId) {
        return webClient.get()
                .uri("/payment/{userId}/balance", userId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(error -> Mono.error(new PaymentProcessingException(
                                        "Ошибка при получении баланса: " + error))))
                .bodyToMono(UserBalanceResponseDto.class)
                .map(userMapper::userBalanceResponseDtoToUserBalance)
                .timeout(TIMEOUT)
                .onErrorMap(WebClientRequestException.class, e ->
                        new PaymentServiceUnavailableException("Сервис платежей недоступен"));
    }

    @Override
    public Mono<Boolean> checkHealth() {
        return webClient.get()
                .uri("/actuator/health")
                .retrieve()
                .bodyToMono(Map.class)
                .map(healthResponse -> {
                    String status = (String) healthResponse.get("status");
                    return "UP".equals(status);
                })
                .timeout(TIMEOUT)
                .onErrorReturn(false);
    }
}