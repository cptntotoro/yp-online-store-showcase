package ru.practicum.exception.user;

import org.springframework.http.HttpStatus;
import ru.practicum.exception.BaseException;

/**
 * Исключение для несуществующего пользователя
 */
public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "Пользователь не найден.");
    }
}