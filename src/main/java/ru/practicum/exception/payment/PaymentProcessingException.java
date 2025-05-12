package ru.practicum.exception.payment;

/**
 * Исключение для ошибки оплаты заказа
 */
public class PaymentProcessingException extends RuntimeException {
    public PaymentProcessingException(String message) {
        super(message);
    }
}
