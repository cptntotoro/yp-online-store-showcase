package ru.practicum.client;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import ru.practicum.client.api.PaymentApi;
import ru.practicum.client.dto.PaymentRequestDto;
import ru.practicum.client.dto.RefundRequestDto;
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

    private final PaymentApi paymentApiClient;

    private final ReactiveClientRegistrationRepository clientRegistrationRepository;
    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    @Override
    public Mono<Void> processPayment(UUID userUuid, UUID orderUuid, BigDecimal total) {
        PaymentRequestDto request = new PaymentRequestDto()
                .userUuid(userUuid)
                .orderUuid(orderUuid)
                .amount(total);

        return paymentApiClient.processPayment(request)
                .onErrorResume(throwable -> Mono.error(new PaymentProcessingException(
                        "Ошибка при обработке платежа: " + throwable)))
                .flatMap(response -> {
                    if (!response.getIsSuccess()) {
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
        RefundRequestDto request = new RefundRequestDto()
                .userUuid(userUuid)
                .orderUuid(orderUuid)
                .amount(total);

        return paymentApiClient.processRefund(request)
                .onErrorResume(throwable -> Mono.error(new PaymentProcessingException(
                        "Ошибка при возврате средств: " + throwable)))
                .flatMap(response -> {
                    if (!response.getIsSuccess()) {
                        return Mono.error(new PaymentProcessingException(
                                !response.getMessage().isEmpty() ? response.getMessage() :
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
        return paymentApiClient.getBalance(userId)
                .onErrorResume(throwable -> Mono.error(new PaymentProcessingException(
                        "Ошибка при получении баланса: " + throwable)))
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