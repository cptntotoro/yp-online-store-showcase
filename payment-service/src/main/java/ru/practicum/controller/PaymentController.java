package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.dto.PaymentRequestDto;
import ru.practicum.dto.PaymentResponseDto;
import ru.practicum.dto.RefundRequestDto;
import ru.practicum.dto.RefundResponseDto;
import ru.practicum.dto.UserBalanceResponseDto;
import ru.practicum.mapper.balance.UserBalanceMapper;
import ru.practicum.mapper.payment.PaymentMapper;
import ru.practicum.service.PaymentService;

import java.util.UUID;

@RestController
@RequestMapping("/payment")
@AllArgsConstructor
public class PaymentController implements PaymentApi {

    /**
     * Сервис оплаты заказа
     */
    private final PaymentService paymentService;

    /**
     * Маппер баланса счета пользователя
     */
    private final UserBalanceMapper userBalanceMapper;

    /**
     * Маппер оплаты
     */
    private final PaymentMapper paymentMapper;

    @GetMapping("/{userId}/balance")
    @Override
    public Mono<UserBalanceResponseDto> getBalance(@PathVariable UUID userId, ServerWebExchange exchange) {
        return paymentService.getUserBalance(userId)
                .map(userBalanceMapper::userBalanceToUserBalanceResponseDto);
    }

    @PostMapping
    @Override
    public Mono<PaymentResponseDto> processPayment(@Valid @RequestBody Mono<PaymentRequestDto> paymentRequestDto, ServerWebExchange exchange) {
        return paymentRequestDto
                .flatMap(request -> paymentService.processPayment(request.getUserUuid(), request.getAmount(), request.getOrderUuid()))
                .map(paymentMapper::paymentResultToPaymentResponse);
    }

    @PostMapping("/refund")
    @Override
    public Mono<RefundResponseDto> processRefund(@RequestBody Mono<RefundRequestDto> refundRequestDto, ServerWebExchange exchange) {
        return refundRequestDto
                .flatMap(request -> paymentService.processRefund(request.getUserUuid(), request.getAmount(), request.getOrderUuid()))
                .map(paymentMapper::paymentResultToRefundResponse);
    }
}
