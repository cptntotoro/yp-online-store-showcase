package ru.practicum.exception.payment;

import org.springframework.http.HttpStatus;
import ru.practicum.exception.BaseException;

public class PaymentServiceUnavailableException extends BaseException {
    public PaymentServiceUnavailableException(String message) {
        super(message, HttpStatus.SERVICE_UNAVAILABLE, "Сервис оплаты недоступен. Попробуйте позже.");
    }
}
