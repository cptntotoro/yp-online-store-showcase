package ru.practicum.exception.order;

/**
 * Исключение для несуществующего заказа
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}