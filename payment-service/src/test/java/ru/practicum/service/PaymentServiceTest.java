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

        userBalanceDao = new UserBalanceDao();
        userBalanceDao.setUserUuid(userId);
        userBalanceDao.setAmount(new BigDecimal("200.00"));

        userBalance = new UserBalance();
        userBalance.setUserUuid(userId);
        userBalance.setAmount(new BigDecimal("200.00"));

        // Мок для успешной транзакции
        PaymentTransactionDao successTransactionDao = new PaymentTransactionDao();
        successTransactionDao.setTransactionUuid(UUID.randomUUID());
        successTransactionDao.setUserUuid(userId);
        successTransactionDao.setOrderUuid(orderId);
        successTransactionDao.setAmount(amount);
        successTransactionDao.setTransactionType(TransactionType.WITHDRAWAL);
        successTransactionDao.setStatus(TransactionStatus.COMPLETED);

        PaymentTransaction successTransaction = new PaymentTransaction();
        successTransaction.setTransactionUuid(successTransactionDao.getTransactionUuid());
        successTransaction.setUserUuid(userId);
        successTransaction.setOrderUuid(orderId);
        successTransaction.setAmount(amount);
        successTransaction.setTransactionType(TransactionType.WITHDRAWAL);
        successTransaction.setStatus(TransactionStatus.COMPLETED);

        // Мок для неудачной транзакции
        failedTransactionDao = new PaymentTransactionDao();
        failedTransactionDao.setTransactionUuid(UUID.randomUUID());
        failedTransactionDao.setUserUuid(userId);
        failedTransactionDao.setOrderUuid(orderId);
        failedTransactionDao.setAmount(amount);
        failedTransactionDao.setTransactionType(TransactionType.WITHDRAWAL);
        failedTransactionDao.setStatus(TransactionStatus.FAILED);

        failedTransaction = new PaymentTransaction();
        failedTransaction.setTransactionUuid(failedTransactionDao.getTransactionUuid());
        failedTransaction.setUserUuid(userId);
        failedTransaction.setOrderUuid(orderId);
        failedTransaction.setAmount(amount);
        failedTransaction.setTransactionType(TransactionType.WITHDRAWAL);
        failedTransaction.setStatus(TransactionStatus.FAILED);

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

        UserBalanceDao updatedBalanceDao = new UserBalanceDao();
        updatedBalanceDao.setUserUuid(userId);
        updatedBalanceDao.setAmount(new BigDecimal("99.50"));

        UserBalance updatedBalance = new UserBalance();
        updatedBalance.setUserUuid(userId);
        updatedBalance.setAmount(new BigDecimal("99.50"));

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
        // Подготовка данных
        UserBalanceDao updatedBalanceDao = new UserBalanceDao();
        updatedBalanceDao.setUserUuid(userId);
        updatedBalanceDao.setAmount(new BigDecimal("300.50"));

        UserBalance updatedBalance = new UserBalance();
        updatedBalance.setUserUuid(userId);
        updatedBalance.setAmount(new BigDecimal("300.50"));

        // Создаем специфичные моки для refund транзакции
        PaymentTransactionDao refundTransactionDao = new PaymentTransactionDao();
        refundTransactionDao.setTransactionUuid(UUID.randomUUID());
        refundTransactionDao.setUserUuid(userId);
        refundTransactionDao.setOrderUuid(orderId);
        refundTransactionDao.setAmount(amount);
        refundTransactionDao.setTransactionType(TransactionType.REFUND);
        refundTransactionDao.setStatus(TransactionStatus.COMPLETED);

        PaymentTransaction refundTransaction = new PaymentTransaction();
        refundTransaction.setTransactionUuid(refundTransactionDao.getTransactionUuid());
        refundTransaction.setUserUuid(userId);
        refundTransaction.setOrderUuid(orderId);
        refundTransaction.setAmount(amount);
        refundTransaction.setTransactionType(TransactionType.REFUND);
        refundTransaction.setStatus(TransactionStatus.COMPLETED);

        // Настройка моков
        when(userBalanceRepository.addToBalance(userId, amount))
                .thenReturn(Mono.just(updatedBalanceDao));
        when(userBalanceMapper.userBalanceDaoToUserBalance(updatedBalanceDao))
                .thenReturn(updatedBalance);

        // Переопределяем маппер для refund случая
        when(paymentTransactionMapper.paymentTransactionToPaymentTransactionDao(argThat(t ->
                t.getTransactionType() == TransactionType.REFUND)))
                .thenReturn(refundTransactionDao);

        when(paymentTransactionMapper.paymentTransactionDaoToPaymentTransaction(argThat(d ->
                d.getTransactionType() == TransactionType.REFUND)))
                .thenReturn(refundTransaction);

        when(transactionRepository.save(argThat(d ->
                d.getTransactionType() == TransactionType.REFUND)))
                .thenReturn(Mono.just(refundTransactionDao));

        // Выполнение и проверки
        StepVerifier.create(paymentService.processRefund(userId, amount, orderId))
                .assertNext(result -> {
                    assertNotNull(result);
                    assertTrue(result.isSuccess());
                    assertEquals("Оплата успешна", result.getMessage());
                    assertNotNull(result.getUpdatedBalance());
                    assertEquals(0, new BigDecimal("300.50").compareTo(result.getUpdatedBalance().getAmount()));
                    assertNotNull(result.getTransaction());
                    assertEquals(TransactionStatus.COMPLETED, result.getTransaction().getStatus());
                    assertEquals(TransactionType.REFUND, result.getTransaction().getTransactionType()); // Проверяем тип транзакции
                })
                .verifyComplete();

        verify(userBalanceRepository).addToBalance(userId, amount);
        verify(transactionRepository).save(argThat(d ->
                d.getTransactionType() == TransactionType.REFUND));
    }

    @Test
    void processRefund_shouldHandleRefundError() {
        when(userBalanceRepository.addToBalance(userId, amount))
                .thenReturn(Mono.error(new RuntimeException("Refund error")));

        StepVerifier.create(paymentService.processRefund(userId, amount, orderId))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Refund error"))
                .verify();

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void processPayment_shouldHandleBalanceUpdateError() {
        when(userBalanceRepository.findByUserUuid(userId))
                .thenReturn(Mono.just(userBalanceDao));

        when(userBalanceRepository.deductBalance(userId, amount))
                .thenReturn(Mono.error(new RuntimeException("Balance update error")));

        StepVerifier.create(paymentService.processPayment(userId, amount, orderId))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Balance update error"))
                .verify();

        verify(transactionRepository, never()).save(any());
    }
}