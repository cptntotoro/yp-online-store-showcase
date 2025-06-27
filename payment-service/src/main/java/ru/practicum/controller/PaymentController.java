package ru.practicum.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.dto.balance.UserBalanceResponseDto;
import ru.practicum.dto.payment.PaymentRequestDto;
import ru.practicum.dto.payment.PaymentResponseDto;
import ru.practicum.dto.refund.RefundRequestDto;
import ru.practicum.dto.refund.RefundResponseDto;
import ru.practicum.mapper.payment.PaymentMapper;
import ru.practicum.service.PaymentService;

import java.util.UUID;

import org.springframework.web.bind.annotation.*;
import ru.practicum.mapper.balance.UserBalanceMapper;

@RestController
@RequestMapping("/payment")
@AllArgsConstructor
public class PaymentController {

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
    public Mono<UserBalanceResponseDto> getBalance(@PathVariable UUID userId) {
        return paymentService.getUserBalance(userId)
                .map(userBalanceMapper::userBalanceToUserBalanceResponseDto);
    }

    @PostMapping
    public Mono<PaymentResponseDto> processPayment(@RequestBody PaymentRequestDto request) {
        return paymentService.processPayment(request.getUserId(), request.getAmount(), request.getOrderId())
                .map(paymentMapper::paymentResultToPaymentResponse);
    }

    @PostMapping("/refund")
    public Mono<RefundResponseDto> processRefund(@RequestBody RefundRequestDto request) {
        return paymentService.processRefund(request.getUserId(), request.getAmount(), request.getOrderId())
                .map(paymentMapper::paymentResultToRefundResponse);
    }
}
