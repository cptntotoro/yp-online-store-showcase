package ru.practicum.exception.cart;

public class IllegalCartStateException extends RuntimeException {
    public IllegalCartStateException(String message) {
        super(message);
    }
}