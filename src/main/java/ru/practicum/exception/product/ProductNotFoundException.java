package ru.practicum.exception.product;

import org.springframework.http.HttpStatus;
import ru.practicum.exception.BaseException;

/**
 * Исключение для несуществующего товара
 */
public class ProductNotFoundException extends BaseException {
    public ProductNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "Товар не найден.");
    }
}