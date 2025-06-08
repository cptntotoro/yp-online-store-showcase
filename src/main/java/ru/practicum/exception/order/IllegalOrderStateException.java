package ru.practicum.exception.order;

import org.springframework.http.HttpStatus;
import ru.practicum.exception.BaseException;

/**
 * Исключение для некорректного состояния заказа
 */
public class IllegalOrderStateException extends BaseException {
    public IllegalOrderStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "Некорректное состояние заказа.");
    }
}