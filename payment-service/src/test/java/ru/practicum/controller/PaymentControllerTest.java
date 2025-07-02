package ru.practicum.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dto.balance.UserBalanceResponseDto;
import ru.practicum.dto.payment.PaymentRequestDto;
import ru.practicum.dto.payment.PaymentResponseDto;
import ru.practicum.dto.refund.RefundRequestDto;
import ru.practicum.dto.refund.RefundResponseDto;
import ru.practicum.mapper.balance.UserBalanceMapper;
import ru.practicum.mapper.payment.PaymentMapper;
import ru.practicum.model.balance.UserBalance;
import ru.practicum.model.payment.PaymentResult;
import ru.practicum.model.transaction.PaymentTransaction;
import ru.practicum.model.transaction.TransactionStatus;
import ru.practicum.model.transaction.TransactionType;
import ru.practicum.service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Mock
    private UserBalanceMapper userBalanceMapper;

    @Mock
    private PaymentMapper paymentMapper;

    @InjectMocks
    private PaymentController paymentController;

    private final UUID userId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final BigDecimal amount = new BigDecimal("100.50");

    @Test
    void getBalance_shouldReturnBalanceResponse() {
        UserBalance userBalance = UserBalance.builder()
                .userUuid(userId)
                .amount(amount)
                .build();

        UserBalanceResponseDto responseDto = UserBalanceResponseDto.builder()
                .balance(amount)
                .build();

        when(paymentService.getUserBalance(userId))
                .thenReturn(Mono.just(userBalance));
        when(userBalanceMapper.userBalanceToUserBalanceResponseDto(userBalance))
                .thenReturn(responseDto);

        StepVerifier.create(paymentController.getBalance(userId))
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(amount, dto.getBalance());
                })
                .verifyComplete();
    }

    @Test
    void processPayment_shouldReturnPaymentResponse() {
        PaymentRequestDto request = new PaymentRequestDto(userId, amount, orderId);
        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionUuid(UUID.randomUUID())
                .userUuid(userId)
                .orderUuid(orderId)
                .amount(amount)
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .build();

        PaymentResult paymentResult = PaymentResult.successfulPaymentResult(transaction,
                UserBalance.builder().userUuid(userId).amount(new BigDecimal("200.00")).build());

        PaymentResponseDto responseDto = PaymentResponseDto.builder()
                .userUuid(userId)
                .transactionUuid(transaction.getTransactionUuid())
                .newBalance(new BigDecimal("200.00"))
                .build();

        when(paymentService.processPayment(userId, amount, orderId))
                .thenReturn(Mono.just(paymentResult));
        when(paymentMapper.paymentResultToPaymentResponse(paymentResult))
                .thenReturn(responseDto);

        StepVerifier.create(paymentController.processPayment(request))
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(userId, dto.getUserUuid());
                    assertEquals(transaction.getTransactionUuid(), dto.getTransactionUuid());
                    assertEquals(0, new BigDecimal("200.00").compareTo(dto.getNewBalance()));
                })
                .verifyComplete();
    }

    @Test
    void processRefund_shouldReturnRefundResponse() {
        RefundRequestDto request = new RefundRequestDto(userId, amount, orderId);
        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionUuid(UUID.randomUUID())
                .userUuid(userId)
                .orderUuid(orderId)
                .amount(amount)
                .transactionType(TransactionType.REFUND)
                .status(TransactionStatus.COMPLETED)
                .build();

        PaymentResult paymentResult = PaymentResult.successfulPaymentResult(transaction,
                UserBalance.builder().userUuid(userId).amount(new BigDecimal("300.50")).build());

        RefundResponseDto responseDto = RefundResponseDto.builder()
                .userUuid(userId)
                .transactionUuid(transaction.getTransactionUuid())
                .newBalance(new BigDecimal("300.50"))
                .isSuccess(true)
                .message("Успешный возврат средств")
                .build();

        when(paymentService.processRefund(userId, amount, orderId))
                .thenReturn(Mono.just(paymentResult));
        when(paymentMapper.paymentResultToRefundResponse(paymentResult))
                .thenReturn(responseDto);

        StepVerifier.create(paymentController.processRefund(request))
                .assertNext(dto -> {
                    assertNotNull(dto);
                    assertEquals(userId, dto.getUserUuid());
                    assertEquals(transaction.getTransactionUuid(), dto.getTransactionUuid());
                    assertEquals(0, new BigDecimal("300.50").compareTo(dto.getNewBalance()));
                    assertTrue(dto.isSuccess());
                    assertEquals("Успешный возврат средств", dto.getMessage());
                })
                .verifyComplete();
    }

    @Test
    void getBalance_shouldHandleError() {
        when(paymentService.getUserBalance(userId))
                .thenReturn(Mono.error(new RuntimeException("Недостаточно средств на счете")));

        StepVerifier.create(paymentController.getBalance(userId))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Недостаточно средств на счете"))
                .verify();
    }

    @Test
    void processPayment_shouldHandlePaymentError() {
        PaymentRequestDto request = new PaymentRequestDto(userId, amount, orderId);
        when(paymentService.processPayment(userId, amount, orderId))
                .thenReturn(Mono.error(new RuntimeException("Недостаточно средств на счете")));

        StepVerifier.create(paymentController.processPayment(request))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Недостаточно средств на счете"))
                .verify();
    }

    @Test
    void processRefund_shouldHandleRefundError() {
        RefundRequestDto request = new RefundRequestDto(userId, amount, orderId);
        when(paymentService.processRefund(userId, amount, orderId))
                .thenReturn(Mono.error(new RuntimeException("Ошибка возврата средств")));

        StepVerifier.create(paymentController.processRefund(request))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Ошибка возврата средств"))
                .verify();
    }
}