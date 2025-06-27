package ru.practicum.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public Mono<ResponseEntity<ApiError>> handleBaseException(BaseException e) {
        ApiError error = new ApiError(
                e.getStatus().toString(),
                e.getMessage() + ". " + e.getReason(),
                LocalDateTime.now()
        );
        return Mono.just(ResponseEntity.status(e.getStatus()).body(error));
    }
}
