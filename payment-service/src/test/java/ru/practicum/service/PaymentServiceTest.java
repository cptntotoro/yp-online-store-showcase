package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.balance.UserBalanceDao;
import ru.practicum.dao.transaction.PaymentTransactionDao;
import ru.practicum.mapper.balance.UserBalanceMapper;
import ru.practicum.mapper.transaction.PaymentTransactionMapper;
import ru.practicum.model.balance.UserBalance;
import ru.practicum.model.transaction.PaymentTransaction;
import ru.practicum.model.transaction.TransactionStatus;
import ru.practicum.model.transaction.TransactionType;
import ru.practicum.repository.balance.UserBalanceRepository;
import ru.practicum.repository.transaction.TransactionRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// TODO: должны быть написаны юнит- и интеграционные тесты на сервис платежей.
@SpringBootTest
@TestPropertySource(properties = {
        "payment.default-balance=15000.00"
})
class PaymentServiceTest {

    @Autowired
    private PaymentServiceImpl paymentService;

    @MockBean
    private UserBalanceRepository userBalanceRepository;

    @MockBean
    private TransactionRepository transactionRepository;

    @MockBean
    private UserBalanceMapper userBalanceMapper;

    @MockBean
    private PaymentTransactionMapper paymentTransactionMapper;

    private UUID userId;
    private UUID orderId;
    private BigDecimal amount;
    private UserBalanceDao userBalanceDao;
    private UserBalance userBalance;
    private PaymentTransactionDao failedTransactionDao;
    private PaymentTransaction failedTransaction;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        amount = new BigDecimal("100.50");

        userBalanceDao = UserBalanceDao.builder()
                .userUuid(userId)
                .amount(new BigDecimal("200.00"))
                .build();

        userBalance = UserBalance.builder()
                .userUuid(userId)
                .amount(new BigDecimal("200.00"))
                .build();

        PaymentTransactionDao successTransactionDao = PaymentTransactionDao.builder()
                .transactionUuid(UUID.randomUUID())
                .userUuid(userId)
                .orderUuid(orderId)
                .amount(amount)
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .build();

        PaymentTransaction successTransaction = PaymentTransaction.builder()
                .transactionUuid(successTransactionDao.getTransactionUuid())
                .userUuid(userId)
                .orderUuid(orderId)
                .amount(amount)
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .build();

        failedTransactionDao = PaymentTransactionDao.builder()
                .transactionUuid(UUID.randomUUID())
                .userUuid(userId)
                .orderUuid(orderId)
                .amount(amount)
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.FAILED)
                .build();

        failedTransaction = PaymentTransaction.builder()
                .transactionUuid(failedTransactionDao.getTransactionUuid())
                .userUuid(userId)
                .orderUuid(orderId)
                .amount(amount)
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.FAILED)
                .build();

        when(userBalanceMapper.userBalanceDaoToUserBalance(any(UserBalanceDao.class)))
                .thenReturn(userBalance);
        when(paymentTransactionMapper.paymentTransactionToPaymentTransactionDao(any(PaymentTransaction.class)))
                .thenAnswer(invocation -> {
                    PaymentTransaction t = invocation.getArgument(0);
                    if (t.getStatus() == TransactionStatus.FAILED) {
                        return failedTransactionDao;
                    }
                    return successTransactionDao;
                });
        when(paymentTransactionMapper.paymentTransactionDaoToPaymentTransaction(any(PaymentTransactionDao.class)))
                .thenAnswer(invocation -> {
                    PaymentTransactionDao dao = invocation.getArgument(0);
                    if (dao.getStatus() == TransactionStatus.FAILED) {
                        return failedTransaction;
                    }
                    return successTransaction;
                });
        when(transactionRepository.save(any(PaymentTransactionDao.class)))
                .thenAnswer(invocation -> {
                    PaymentTransactionDao dao = invocation.getArgument(0);
                    if (dao.getStatus() == TransactionStatus.FAILED) {
                        return Mono.just(failedTransactionDao);
                    }
                    return Mono.just(successTransactionDao);
                });
    }

    @Test
    void getUserBalance_shouldReturnDefaultBalanceWhenNotFound() {
        when(userBalanceRepository.findByUserUuid(userId))
                .thenReturn(Mono.empty());

        StepVerifier.create(paymentService.getUserBalance(userId))
                .assertNext(balance -> {
                    assertNotNull(balance);
                    assertEquals(userId, balance.getUserUuid());
                    assertEquals(new BigDecimal("15000.00"), balance.getAmount());
                })
                .verifyComplete();
    }

    @Test
    void processPayment_shouldReturnFailedResultWhenInsufficientFunds() {
        BigDecimal insufficientAmount = new BigDecimal("300.00");
        when(userBalanceRepository.findByUserUuid(userId))
                .thenReturn(Mono.just(userBalanceDao));

        StepVerifier.create(paymentService.processPayment(userId, insufficientAmount, orderId))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertFalse(result.isSuccess());
                    assertEquals("Недостаточно средств на счете", result.getMessage());
                    assertEquals(userBalance, result.getUpdatedBalance());
                    assertNotNull(result.getTransaction());
                    assertEquals(TransactionStatus.FAILED, result.getTransaction().getStatus());
                })
                .verifyComplete();

        verify(userBalanceRepository, never()).deductBalance(any(), any());
    }

    @Test
    void processPayment_shouldProcessSuccessfulPayment() {
        when(userBalanceRepository.findByUserUuid(userId))
                .thenReturn(Mono.just(userBalanceDao));

        UserBalanceDao updatedBalanceDao = UserBalanceDao.builder()
                .userUuid(userId)
                .amount(new BigDecimal("99.50"))
                .build();

        UserBalance updatedBalance = UserBalance.builder()
                .userUuid(userId)
                .amount(new BigDecimal("99.50"))
                .build();

        when(userBalanceRepository.deductBalance(userId, amount))
                .thenReturn(Mono.just(updatedBalanceDao));
        when(userBalanceMapper.userBalanceDaoToUserBalance(updatedBalanceDao))
                .thenReturn(updatedBalance);

        StepVerifier.create(paymentService.processPayment(userId, amount, orderId))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.isSuccess());
                    assertEquals("Оплата успешна", result.getMessage());
                    assertNotNull(result.getUpdatedBalance());
                    assertEquals(0, new BigDecimal("99.50").compareTo(result.getUpdatedBalance().getAmount()));
                    assertNotNull(result.getTransaction());
                    assertEquals(TransactionStatus.COMPLETED, result.getTransaction().getStatus());
                })
                .verifyComplete();

        verify(userBalanceRepository).deductBalance(userId, amount);
        verify(transactionRepository).save(any(PaymentTransactionDao.class));
    }

    @Test
    void processRefund_shouldProcessSuccessfulRefund() {
        UserBalanceDao updatedBalanceDao = UserBalanceDao.builder()
                .userUuid(userId)
                .amount(new BigDecimal("300.50"))
                .build();

        UserBalance updatedBalance = UserBalance.builder()
                .userUuid(userId)
                .amount(new BigDecimal("300.50"))
                .build();

        UUID transactionId = UUID.randomUUID();
        PaymentTransactionDao refundTransactionDao = PaymentTransactionDao.builder()
                .transactionUuid(transactionId)
                .userUuid(userId)
                .orderUuid(orderId)
                .amount(amount)
                .transactionType(TransactionType.REFUND)
                .status(TransactionStatus.COMPLETED)
                .build();

        PaymentTransaction refundTransaction = PaymentTransaction.builder()
                .transactionUuid(transactionId)
                .userUuid(userId)
                .orderUuid(orderId)
                .amount(amount)
                .transactionType(TransactionType.REFUND)
                .status(TransactionStatus.COMPLETED)
                .build();

        when(userBalanceRepository.addToBalance(userId, amount))
                .thenReturn(Mono.just(updatedBalanceDao));
        when(userBalanceMapper.userBalanceDaoToUserBalance(updatedBalanceDao))
                .thenReturn(updatedBalance);

        when(paymentTransactionMapper.paymentTransactionToPaymentTransactionDao(argThat(t ->
                t.getTransactionType() == TransactionType.REFUND)))
                .thenReturn(refundTransactionDao);

        when(paymentTransactionMapper.paymentTransactionDaoToPaymentTransaction(argThat(d ->
                d.getTransactionType() == TransactionType.REFUND)))
                .thenReturn(refundTransaction);

        when(transactionRepository.save(argThat(d ->
                d.getTransactionType() == TransactionType.REFUND)))
                .thenReturn(Mono.just(refundTransactionDao));

        StepVerifier.create(paymentService.processRefund(userId, amount, orderId))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.isSuccess());
                    assertEquals("Оплата успешна", result.getMessage());
                    assertNotNull(result.getUpdatedBalance());
                    assertEquals(0, new BigDecimal("300.50").compareTo(result.getUpdatedBalance().getAmount()));
                    assertNotNull(result.getTransaction());
                    assertEquals(TransactionStatus.COMPLETED, result.getTransaction().getStatus());
                    assertEquals(TransactionType.REFUND, result.getTransaction().getTransactionType());
                })
                .verifyComplete();

        verify(userBalanceRepository).addToBalance(userId, amount);
        verify(transactionRepository).save(argThat(d ->
                d.getTransactionType() == TransactionType.REFUND));
    }

    @Test
    void processRefund_shouldHandleRefundError() {
        when(userBalanceRepository.addToBalance(userId, amount))
                .thenReturn(Mono.error(new RuntimeException("Ошибка возврата средств")));

        StepVerifier.create(paymentService.processRefund(userId, amount, orderId))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Ошибка возврата средств"))
                .verify();

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processPayment_shouldHandleBalanceUpdateError() {
        when(userBalanceRepository.findByUserUuid(userId))
                .thenReturn(Mono.just(userBalanceDao));

        when(userBalanceRepository.deductBalance(userId, amount))
                .thenReturn(Mono.error(new RuntimeException("Ошибка обновления баланса средств пользователя")));

        StepVerifier.create(paymentService.processPayment(userId, amount, orderId))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Ошибка обновления баланса средств пользователя"))
                .verify();

        verify(transactionRepository, never()).save(any());
    }
}