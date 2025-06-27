package ru.practicum.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO ответа оплаты заказа
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDto {

    /**
     * Идентификатор пользователя
     */
    private UUID userUuid;

    /**
     * Флаг успеха
     */
    private boolean isSuccess;

    /**
     * Идентификатор транзакции
     */
    private UUID transactionUuid;

    /**
     * Новое значение баланса счета пользователя
     */
    private BigDecimal newBalance;
}
