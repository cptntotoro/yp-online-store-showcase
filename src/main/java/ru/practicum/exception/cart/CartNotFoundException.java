package ru.practicum.exception.cart;

import org.springframework.http.HttpStatus;
import ru.practicum.exception.BaseException;

/**
 * Исключение для несуществующей корзины
 */
public class CartNotFoundException extends BaseException {
    public CartNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "Корзина не найдена.");
    }
}