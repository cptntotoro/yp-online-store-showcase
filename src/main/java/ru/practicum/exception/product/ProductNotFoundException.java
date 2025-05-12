package ru.practicum.exception.product;

/**
 * Исключение для несуществующего товара
 */
public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}