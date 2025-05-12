package ru.practicum.service.payment;

import java.util.UUID;

/**
 * Сервис оплаты заказа
 */
public interface PaymentService {

    /**
     * Эмуляция процесса оплаты
     *
     * @param userUuid Идентификатор пользователя
     * @param orderUuid Идентификатор заказа
     * @param cardNumber Номер карты
     */
    void checkout(UUID userUuid, UUID orderUuid, String cardNumber);
}
