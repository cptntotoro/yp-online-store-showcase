package ru.practicum.dto.payment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @NotNull(message = "Идентификатор пользователя не может быть null")
    private UUID userUuid;

    /**
     * Сумма к оплате
     */
    @NotNull(message = "Сумма платежа не может быть null")
    @Positive(message = "Сумма платежа должна быть положительной")
    private BigDecimal amount;

    /**
     * Идентификатор заказа
     */
    @NotNull(message = "Идентификатор заказа не может быть null")
    private UUID orderUuid;
}
