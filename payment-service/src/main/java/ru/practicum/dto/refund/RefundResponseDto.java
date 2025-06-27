package ru.practicum.dto.refund;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO ответа на возврат средств
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponseDto {

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

    /**
     * Сообщение о результате
     */
    private String message;
}
