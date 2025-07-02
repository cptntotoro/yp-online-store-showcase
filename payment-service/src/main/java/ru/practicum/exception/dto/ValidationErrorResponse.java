package ru.practicum.exception.dto;

import java.util.List;

public record ValidationErrorResponse (int status, String message, List<FieldError> fieldErrors) {
    public record FieldError (String objectName, String field, String message) {
    }
}
