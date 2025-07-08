package ru.practicum.exception.auth;

import org.springframework.http.HttpStatus;
import ru.practicum.exception.BaseException;

public class UserAlreadyExistsException extends BaseException {
    public UserAlreadyExistsException(String message) {
        super(message, HttpStatus.NOT_FOUND, "Корзина не найдена.");
    }
}