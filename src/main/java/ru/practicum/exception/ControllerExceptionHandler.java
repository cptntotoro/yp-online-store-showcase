package ru.practicum.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.exception.cart.CartNotFoundException;
import ru.practicum.exception.cart.IllegalCartStateException;
import ru.practicum.exception.order.IllegalOrderStateException;
import ru.practicum.exception.order.OrderNotFoundException;
import ru.practicum.exception.payment.PaymentProcessingException;
import ru.practicum.exception.product.ProductNotFoundException;
import ru.practicum.exception.user.UserNotFoundException;

import java.time.LocalDateTime;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String exceptionNotFound(ProductNotFoundException e, Model model) {
        String reason = "Товар не найден. " + e.getMessage();
        ApiError error = new ApiError(HttpStatus.NOT_FOUND.toString(), reason, e.getMessage(), LocalDateTime.now());
        model.addAttribute("error", error);
        return "error";
    }

    @ExceptionHandler(OrderNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String exceptionNotFound(OrderNotFoundException e, Model model) {
        String reason = "Заказ не найден. " + e.getMessage();
        ApiError error = new ApiError(HttpStatus.NOT_FOUND.toString(), reason, e.getMessage(), LocalDateTime.now());
        model.addAttribute("error", error);
        return "error";
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String exceptionNotFound(UserNotFoundException e, Model model) {
        String reason = "Пользователь не найден. " + e.getMessage();
        ApiError error = new ApiError(HttpStatus.NOT_FOUND.toString(), reason, e.getMessage(), LocalDateTime.now());
        model.addAttribute("error", error);
        return "error";
    }

    @ExceptionHandler(CartNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    public String exceptionNotFound(CartNotFoundException e, Model model) {
        String reason = "Корзина не найдена. " + e.getMessage();
        ApiError error = new ApiError(HttpStatus.NOT_FOUND.toString(), reason, e.getMessage(), LocalDateTime.now());
        model.addAttribute("error", error);
        return "error";
    }

    @ExceptionHandler(IllegalOrderStateException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String exceptionIllegalState(IllegalOrderStateException e, Model model) {
        String reason = "Некорректное состояние заказа. " + e.getMessage();
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.toString(), reason, e.getMessage(), LocalDateTime.now());
        model.addAttribute("error", error);
        return "error";
    }

    @ExceptionHandler(IllegalCartStateException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public String exceptionIllegalState(IllegalCartStateException e, Model model) {
        String reason = "Некорректное состояние корзины. " + e.getMessage();
        ApiError error = new ApiError(HttpStatus.BAD_REQUEST.toString(), reason, e.getMessage(), LocalDateTime.now());
        model.addAttribute("error", error);
        return "error";
    }

    @ExceptionHandler(PaymentProcessingException.class)
    @ResponseStatus(value = HttpStatus.PAYMENT_REQUIRED)
    public String exceptionPaymentProcessing(PaymentProcessingException e, Model model) {
        String reason = "Ошибка оплаты заказа. " + e.getMessage();
        ApiError error = new ApiError(HttpStatus.PAYMENT_REQUIRED.toString(), reason, e.getMessage(), LocalDateTime.now());
        model.addAttribute("error", error);
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public String exceptionNotFound(Exception e, Model model) {
        ApiError error = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), e.toString(), "Возникла проблема на стороне сервера", LocalDateTime.now());
        model.addAttribute("error", error);
        return "error";
    }
}
