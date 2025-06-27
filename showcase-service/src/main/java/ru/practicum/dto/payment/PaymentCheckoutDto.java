package ru.practicum.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO данных для оплаты заказа
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCheckoutDto {
    /**
     * Номер карты
     */
    private String cardNumber;
}
