package ru.practicum.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public String exceptionNotFound(BaseException e, Model model) {
        String reason = "Товар не найден. " + e.getMessage();
        ApiError error = new ApiError(e.getStatus().toString(), reason, e.getMessage(), LocalDateTime.now());
        model.addAttribute("error", error);
        return "error";
    }
}
