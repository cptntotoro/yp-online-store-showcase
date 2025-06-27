package ru.practicum.service;

import reactor.core.publisher.Mono;
import ru.practicum.model.balance.UserBalance;
import ru.practicum.model.payment.PaymentResult;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Сервис оплаты заказа
 */
public interface PaymentService {

    /**
     * Получить баланс счета пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Баланс счета пользователя
     */
    Mono<UserBalance> getUserBalance(UUID userUuid);

    /**
     * Обработать оплату
     *
     * @param userUuid Идентификатор пользователя
     * @param amount Сумма оплаты
     * @param orderId Идентификатор заказа
     * @return Результат оплаты
     */
    Mono<PaymentResult> processPayment(UUID userUuid, BigDecimal amount, UUID orderId);

    /**
     * Обработать возврат средств
     *
     * @param userUuid Идентификатор пользователя
     * @param amount Сумма возврата
     * @param orderId Идентификатор заказа
     * @return Результат оплаты
     */
    Mono<PaymentResult> processRefund(UUID userUuid, BigDecimal amount, UUID orderId);
}
