package ru.practicum.exception.user;

/**
 * Исключение для несуществующего пользователя
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}