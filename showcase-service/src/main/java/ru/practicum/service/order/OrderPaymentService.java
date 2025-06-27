package ru.practicum.service.order;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Сервис оплаты заказов
 */
public interface OrderPaymentService {

    /**
     * Провести оплату заказа
     *
     * @param userUuid Идентификатор пользователя
     * @param orderUuid Идентификатор заказа
     * @param cardNumber Номер карты пользователя
     */
    Mono<Void> processPayment(UUID userUuid, UUID orderUuid, String cardNumber);

    /**
     * Провести отмену заказа
     *
     * @param userUuid Идентификатор пользователя
     * @param orderUuid Идентификатор заказа
     */
    Mono<Void> cancel(UUID userUuid, UUID orderUuid);

    /**
     * Проверить, достаточно ли средств на счете пользователя для оплаты заказа
     *
     * @param userId Идентификатор пользователя
     * @param orderId Идентификатор заказа
     * @return Да / Нет
     */
    Mono<Boolean> isBalanceSufficient(UUID userId, UUID orderId);

    /**
     * Проверить, активен ли сервис оплаты
     *
     * @return Да/ Нет
     */
    Mono<Boolean> checkHealth();
}
