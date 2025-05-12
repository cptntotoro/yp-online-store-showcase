package ru.practicum.exception.cart;

/**
 * Исключение для несуществующей корзины
 */
public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String message) {
        super(message);
    }
}