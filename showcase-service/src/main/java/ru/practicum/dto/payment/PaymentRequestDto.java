package ru.practicum.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO запроса на оплату заказа
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDto {
    /**
     * Идентификатор пользователя
     */
    private UUID userId;

    /**
     * Сумма к оплате
     */
    private BigDecimal amount;

    /**
     * Идентификатор заказа
     */
    private UUID orderId;
}
