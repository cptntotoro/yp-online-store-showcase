package ru.practicum.dto.refund;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO запроса на возврат средств
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequestDto {

    /**
     * Идентификатор пользователя
     */
    private UUID userUuid;

    /**
     * Сумма возврата
     */
    private BigDecimal amount;

    /**
     * Идентификатор заказа
     */
    private UUID orderUuid;
}