package ru.practicum.model.transaction;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Транзакция оплаты
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    /**
     * Идентификатор
     */
    private UUID transactionUuid;

    /**
     * Идентификатор пользователя
     */
    private UUID userUuid;

    /**
     * Идентификатор заказа
     */
    private UUID orderUuid;

    /**
     * Сумма
     */
    private BigDecimal amount;

    /**
     * Тип транзакции
     */
    private TransactionType transactionType;

    /**
     * Статус транзакции
     */
    private TransactionStatus status;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;
}
