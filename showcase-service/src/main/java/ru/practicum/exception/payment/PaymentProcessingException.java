package ru.practicum.exception.payment;

import org.springframework.http.HttpStatus;
import ru.practicum.exception.BaseException;

/**
 * Исключение для ошибки оплаты заказа
 */
public class PaymentProcessingException extends BaseException {
    public PaymentProcessingException(String message) {
        super(message, HttpStatus.PAYMENT_REQUIRED, "Ошибка оплаты заказа.");
    }
}
