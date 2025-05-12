package ru.practicum.exception.cart;

/**
 * Исключение для некорректного состояния корзины
 */
public class IllegalCartStateException extends RuntimeException {
    public IllegalCartStateException(String message) {
        super(message);
    }
}