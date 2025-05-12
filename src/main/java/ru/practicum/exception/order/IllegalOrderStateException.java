package ru.practicum.exception.order;

/**
 * Исключение для некорректного состояния заказа
 */
public class IllegalOrderStateException extends RuntimeException {
    public IllegalOrderStateException(String message) {
        super(message);
    }
}