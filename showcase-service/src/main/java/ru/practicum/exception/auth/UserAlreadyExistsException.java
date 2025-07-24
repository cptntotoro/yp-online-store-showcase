package ru.practicum.exception.auth;

import org.springframework.http.HttpStatus;
import ru.practicum.exception.BaseException;

/**
 * Исключение для уже существующего пользователя
 */
public class UserAlreadyExistsException extends BaseException {
    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "Такой пользователь уже существует.");
    }
}