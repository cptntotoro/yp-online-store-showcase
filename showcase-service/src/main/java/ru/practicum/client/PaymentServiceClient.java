package ru.practicum.client;

import reactor.core.publisher.Mono;
import ru.practicum.model.balance.UserBalance;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Клиент для сервиса оплаты
 */
public interface PaymentServiceClient {

    /**
     * Отправить запрос на обработку оплаты
     *
     * @param userUuid Идентификатор пользователя
     * @param orderUuid Идентификатор заказа
     * @param total Сумма платежа
     */
    Mono<Void> processPayment(UUID userUuid, UUID orderUuid, BigDecimal total);

    /**
     * Отправить запрос на обработку возврата
     *
     * @param userUuid Идентификатор пользователя
     * @param orderUuid Идентификатор заказа
     * @param total Сумма возврата
     */
    Mono<Void> processRefund(UUID userUuid, UUID orderUuid, BigDecimal total);

    /**
     * Получить значение баланса счета пользователя по его идентификатору
     *
     * @param userUuid Идентификатор пользователя
     * @return Баланс счета пользователя
     */
    Mono<UserBalance> getBalance(UUID userUuid);

    /**
     * Проверить доступность сервиса
     *
     * @return Да / Нет
     */
    Mono<Boolean> checkHealth();
}
