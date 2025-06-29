//package ru.practicum.service.payment;
//
//import io.netty.handler.timeout.TimeoutException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//import ru.practicum.exception.order.IllegalOrderStateException;
//import ru.practicum.exception.payment.PaymentProcessingException;
//import ru.practicum.exception.payment.PaymentServiceUnavailableException;
//import ru.practicum.model.balance.UserBalance;
//import ru.practicum.model.order.OrderStatus;
//import ru.practicum.service.order.OrderService;
//
//import java.math.BigDecimal;
//import java.time.Duration;
//import java.util.Map;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//public class PaymentServiceImpl implements PaymentService {
//    /**
//     * Сервис управления заказами
//     */
//    private final OrderService orderService;
//
//    private final WebClient paymentWebClient;
//
//    private static final Duration PAYMENT_SERVICE_TIMEOUT = Duration.ofSeconds(3);
//
//    /**
//     * Номер карты - 16 цифр
//     */
//    private static final String CARD_FORMAT_REGEX = "\\d{16}";
//
////    @Override
////    public Mono<Void> checkout(UUID userUuid, UUID orderUuid, String cardNumber) {
////        return Mono.defer(() -> {
////            if (cardNumber == null || !cardNumber.matches(CARD_FORMAT_REGEX)) {
////                return Mono.error(new PaymentProcessingException("Некорректный номер карты"));
////            }
////
////            return orderService.getByUuid(userUuid, orderUuid)
////                    .flatMap(order -> {
////                        if (order.getStatus() != OrderStatus.CREATED) {
////                            return Mono.error(new IllegalOrderStateException(
////                                    "Заказ уже обработан или отменен"));
////                        }
////
////                        return paymentWebClient.post()
////                                .uri("/payment")
////                                .bodyValue(Map.of(
////                                        "userId", userUuid,
////                                        "amount", order.getTotalPrice(),
////                                        "orderId", orderUuid
////                                ))
////                                .retrieve()
////                                .onStatus(HttpStatusCode::isError, response ->
////                                        response.bodyToMono(String.class)
////                                                .flatMap(error -> Mono.error(new PaymentProcessingException(
////                                                        "Ошибка при обработке платежа: " + error))))
////                                .bodyToMono(Void.class)
////                                .timeout(PAYMENT_SERVICE_TIMEOUT)
////                                .onErrorMap(TimeoutException.class, e ->
////                                        new PaymentServiceUnavailableException("Сервис платежей недоступен"))
////                                .then(orderService.checkout(userUuid, orderUuid));
////                    });
////        });
////    }
//
////    @Override
////    public Mono<Boolean> isBalanceSufficient(UUID userId, UUID orderUuid) {
////        return orderService.getByUuid(userId, orderUuid)
////                .flatMap(order ->
////                        paymentWebClient.get()
////                                .uri("/payment/{userId}/balance", userId)
////                                .retrieve()
////                                .onStatus(HttpStatusCode::isError, response ->
////                                        response.bodyToMono(String.class)
////                                                .flatMap(error -> Mono.error(new PaymentProcessingException(
////                                                        "Ошибка при получении баланса: " + error))))
////                                .bodyToMono(UserBalance.class)
////                                .timeout(PAYMENT_SERVICE_TIMEOUT)
////                                .map(balance -> balance.getBalance().compareTo(order.getTotalPrice()) >= 0)
////                                .onErrorResume(e -> Mono.error(new PaymentServiceUnavailableException(
////                                        "Сервис платежей недоступен")))
////                );
////    }
//
////    @Override
////    public Mono<Void> processRefund(UUID userId, BigDecimal amount, UUID orderId) {
////        return paymentWebClient.post()
////                .uri("/payment/refund")
////                .bodyValue(Map.of(
////                        "userId", userId,
////                        "amount", amount,
////                        "orderId", orderId
////                ))
////                .retrieve()
////                .onStatus(HttpStatusCode::isError, response ->
////                        response.bodyToMono(String.class)
////                                .flatMap(error -> Mono.error(new PaymentProcessingException(
////                                        "Ошибка при возврате средств: " + error))))
////                .bodyToMono(Void.class)
////                .timeout(PAYMENT_SERVICE_TIMEOUT)
////                .onErrorMap(TimeoutException.class, e ->
////                        new PaymentServiceUnavailableException("Сервис платежей недоступен"));
////    }
//}
