package ru.practicum.service.payment;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Сервис оплаты заказа
 */
public interface PaymentService {

    /**
     * Эмуляция процесса оплаты заказа
     *
     * @param userUuid   Идентификатор пользователя
     * @param orderUuid  Идентификатор заказа
     * @param cardNumber Номер карты
     */
    Mono<Void> checkout(UUID userUuid, UUID orderUuid, String cardNumber);
}
