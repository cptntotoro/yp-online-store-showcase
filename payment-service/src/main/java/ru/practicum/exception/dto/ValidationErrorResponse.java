package ru.practicum.exception.dto;

import java.util.List;

/**
 * DTO ответа в случае ошибки валидации сервиса
 *
 * @param status Статус
 * @param message Сообщение
 * @param fieldErrors Поля ошибки
 */
public record ValidationErrorResponse (int status, String message, List<FieldError> fieldErrors) {
    public record FieldError (String objectName, String field, String message) {
    }
}
