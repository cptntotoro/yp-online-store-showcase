package ru.practicum.exception.cart;

import org.springframework.http.HttpStatus;
import ru.practicum.exception.BaseException;

/**
 * Исключение для некорректного состояния корзины
 */
public class IllegalCartStateException extends BaseException {
    public IllegalCartStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "Некорректное состояние корзины.");
    }
}