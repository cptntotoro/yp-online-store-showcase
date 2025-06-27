package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.balance.UserBalanceDao;
import ru.practicum.dao.transaction.PaymentTransactionDao;
import ru.practicum.mapper.balance.UserBalanceMapper;
import ru.practicum.mapper.transaction.PaymentTransactionMapper;
import ru.practicum.model.balance.UserBalance;
import ru.practicum.model.payment.PaymentResult;
import ru.practicum.model.transaction.PaymentTransaction;
import ru.practicum.model.transaction.TransactionStatus;
import ru.practicum.model.transaction.TransactionType;
import ru.practicum.repository.balance.UserBalanceRepository;
import ru.practicum.repository.transaction.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {
        "payment.default-balance=15000.00"
})
class PaymentServiceTest {

    @Mock
    private UserBalanceRepository userBalanceRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserBalanceMapper userBalanceMapper;

    @Mock
    private PaymentTransactionMapper paymentTransactionMapper;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private final UUID testUserId = UUID.randomUUID();
    private final UUID testOrderId = UUID.randomUUID();
    private final BigDecimal testAmount = BigDecimal.valueOf(100.50);
    private final BigDecimal defaultBalance = BigDecimal.valueOf(1000.00);
    private final LocalDateTime now = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        paymentService.defaultBalance = defaultBalance;
    }

    @Test
    void getUserBalance_shouldReturnExistingBalance() {
        UserBalanceDao balanceDao = new UserBalanceDao(
                UUID.randomUUID(), testUserId, testAmount, now, now
        );
        UserBalance expectedBalance = new UserBalance(
                balanceDao.getUuid(), testUserId, testAmount, now, now
        );

        when(userBalanceRepository.findByUserUuid(testUserId)).thenReturn(Mono.just(balanceDao));
        when(userBalanceMapper.userBalanceDaoToUserBalance(balanceDao)).thenReturn(expectedBalance);

        Mono<UserBalance> result = paymentService.getUserBalance(testUserId);

        StepVerifier.create(result)
                .expectNext(expectedBalance)
                .verifyComplete();

        verify(userBalanceRepository, never()).create(any(), any());
    }

    @Test
    void getUserBalance_shouldCreateNewBalanceIfNotExists() {
        UserBalanceDao newBalanceDao = new UserBalanceDao(
                UUID.randomUUID(), testUserId, defaultBalance, now, now
        );
        UserBalance expectedBalance = new UserBalance(
                newBalanceDao.getUuid(), testUserId, defaultBalance, now, now
        );

        when(userBalanceRepository.findByUserUuid(testUserId)).thenReturn(Mono.empty());
        when(userBalanceRepository.create(testUserId, defaultBalance)).thenReturn(Mono.just(newBalanceDao));
        when(userBalanceMapper.userBalanceDaoToUserBalance(newBalanceDao)).thenReturn(expectedBalance);

        Mono<UserBalance> result = paymentService.getUserBalance(testUserId);

        StepVerifier.create(result)
                .expectNext(expectedBalance)
                .verifyComplete();
    }

    @Test
    void processPayment_shouldSuccessfullyProcessPayment() {
        BigDecimal initialBalance = BigDecimal.valueOf(500.00);
        BigDecimal updatedBalance = initialBalance.subtract(testAmount);
        UUID balanceUuid = UUID.randomUUID();

        UserBalanceDao balanceDao = new UserBalanceDao(
                balanceUuid, testUserId, initialBalance, now, now
        );
        UserBalanceDao updatedBalanceDao = new UserBalanceDao(
                balanceUuid, testUserId, updatedBalance, now, now
        );
        UserBalance userBalance = new UserBalance(
                balanceUuid, testUserId, initialBalance, now, now
        );
        UserBalance updatedUserBalance = new UserBalance(
                balanceUuid, testUserId, updatedBalance, now, now
        );

        PaymentTransaction transaction = PaymentTransaction.builder()
                .userUuid(testUserId)
                .orderUuid(testOrderId)
                .amount(testAmount)
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.COMPLETED)
                .build();

        PaymentTransactionDao transactionDao = new PaymentTransactionDao();

        when(userBalanceRepository.findByUserUuid(testUserId)).thenReturn(Mono.just(balanceDao));
        when(userBalanceMapper.userBalanceDaoToUserBalance(balanceDao)).thenReturn(userBalance);
        when(userBalanceRepository.deductBalance(testUserId, testAmount)).thenReturn(Mono.just(updatedBalanceDao));
        when(userBalanceMapper.userBalanceDaoToUserBalance(updatedBalanceDao)).thenReturn(updatedUserBalance);
        when(paymentTransactionMapper.paymentTransactionToPaymentTransactionDao(any())).thenReturn(transactionDao);
        when(transactionRepository.save(transactionDao)).thenReturn(Mono.just(transactionDao));
        when(paymentTransactionMapper.paymentTransactionDaoToPaymentTransaction(transactionDao)).thenReturn(transaction);

        Mono<PaymentResult> result = paymentService.processPayment(testUserId, testAmount, testOrderId);

        StepVerifier.create(result)
                .assertNext(paymentResult -> {
                    assertTrue(paymentResult.isSuccess());
                    assertEquals(transaction, paymentResult.getTransaction());
                    assertEquals(updatedUserBalance, paymentResult.getUpdatedBalance());
                    assertEquals("Оплата успешна", paymentResult.getMessage());
                })
                .verifyComplete();
    }

    @Test
    void processPayment_shouldFailWhenInsufficientFunds() {
        BigDecimal insufficientAmount = BigDecimal.valueOf(1000.00);
        UUID balanceUuid = UUID.randomUUID();

        UserBalanceDao balanceDao = new UserBalanceDao(
                balanceUuid, testUserId, testAmount, now, now
        );
        UserBalance userBalance = new UserBalance(
                balanceUuid, testUserId, testAmount, now, now
        );

        PaymentTransaction transaction = PaymentTransaction.builder()
                .userUuid(testUserId)
                .orderUuid(testOrderId)
                .amount(insufficientAmount)
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.FAILED)
                .build();

        PaymentTransactionDao transactionDao = new PaymentTransactionDao();

        when(userBalanceRepository.findByUserUuid(testUserId)).thenReturn(Mono.just(balanceDao));
        when(userBalanceMapper.userBalanceDaoToUserBalance(balanceDao)).thenReturn(userBalance);
        when(paymentTransactionMapper.paymentTransactionToPaymentTransactionDao(any())).thenReturn(transactionDao);
        when(transactionRepository.save(transactionDao)).thenReturn(Mono.just(transactionDao));
        when(paymentTransactionMapper.paymentTransactionDaoToPaymentTransaction(transactionDao)).thenReturn(transaction);

        Mono<PaymentResult> result = paymentService.processPayment(testUserId, insufficientAmount, testOrderId);

        StepVerifier.create(result)
                .assertNext(paymentResult -> {
                    assertFalse(paymentResult.isSuccess());
                    assertEquals(transaction, paymentResult.getTransaction());
                    assertEquals(userBalance, paymentResult.getUpdatedBalance());
                    assertEquals("Недостаточно средств на счете", paymentResult.getMessage());
                })
                .verifyComplete();

        verify(userBalanceRepository, never()).deductBalance(any(), any());
    }

    @Test
    void processPayment_shouldFailWhenNegativeAmount() {
        BigDecimal negativeAmount = BigDecimal.valueOf(-100.00);
        UUID transactionUuid = UUID.randomUUID();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionUuid(transactionUuid)
                .userUuid(testUserId)
                .orderUuid(testOrderId)
                .amount(negativeAmount)
                .transactionType(TransactionType.WITHDRAWAL)
                .status(TransactionStatus.FAILED)
                .createdAt(now)
                .build();

        PaymentTransactionDao transactionDao = new PaymentTransactionDao();
        when(paymentTransactionMapper.paymentTransactionToPaymentTransactionDao(any()))
                .thenReturn(transactionDao);
        when(transactionRepository.save(transactionDao))
                .thenReturn(Mono.just(transactionDao));
        when(paymentTransactionMapper.paymentTransactionDaoToPaymentTransaction(transactionDao))
                .thenReturn(transaction);

        UserBalance balance = new UserBalance(UUID.randomUUID(), testUserId, defaultBalance, now, now);
        when(userBalanceRepository.findByUserUuid(testUserId))
                .thenReturn(Mono.just(new UserBalanceDao(balance.getUuid(), testUserId, defaultBalance, now, now)));
        when(userBalanceMapper.userBalanceDaoToUserBalance(any()))
                .thenReturn(balance);

        Mono<PaymentResult> result = paymentService.processPayment(testUserId, negativeAmount, testOrderId);

        StepVerifier.create(result)
                .assertNext(paymentResult -> {
                    assertFalse(paymentResult.isSuccess());
                    assertEquals(TransactionStatus.FAILED, paymentResult.getTransaction().getStatus());
                    assertEquals("Сумма платежа должна быть положительной", paymentResult.getMessage());
                })
                .verifyComplete();
    }

    @Test
    void processRefund_shouldSuccessfullyProcessRefund() {
        BigDecimal initialBalance = BigDecimal.valueOf(500.00);
        BigDecimal updatedBalance = initialBalance.add(testAmount);
        UUID balanceUuid = UUID.randomUUID();
        UUID transactionUuid = UUID.randomUUID();

        UserBalanceDao balanceDao = new UserBalanceDao(
                balanceUuid, testUserId, initialBalance, now, now
        );
        UserBalanceDao updatedBalanceDao = new UserBalanceDao(
                balanceUuid, testUserId, updatedBalance, now, now
        );
        UserBalance userBalance = new UserBalance(
                balanceUuid, testUserId, initialBalance, now, now
        );
        UserBalance updatedUserBalance = new UserBalance(
                balanceUuid, testUserId, updatedBalance, now, now
        );

        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionUuid(transactionUuid)
                .userUuid(testUserId)
                .orderUuid(testOrderId)
                .amount(testAmount)
                .transactionType(TransactionType.REFUND)
                .status(TransactionStatus.COMPLETED)
                .createdAt(now)
                .build();

        PaymentTransactionDao transactionDao = new PaymentTransactionDao();
        when(paymentTransactionMapper.paymentTransactionToPaymentTransactionDao(any()))
                .thenReturn(transactionDao);
        when(transactionRepository.save(transactionDao))
                .thenReturn(Mono.just(transactionDao));
        when(paymentTransactionMapper.paymentTransactionDaoToPaymentTransaction(transactionDao))
                .thenReturn(transaction);

        when(userBalanceRepository.findByUserUuid(testUserId)).thenReturn(Mono.just(balanceDao));
        when(userBalanceMapper.userBalanceDaoToUserBalance(balanceDao)).thenReturn(userBalance);
        when(userBalanceRepository.addToBalance(testUserId, testAmount)).thenReturn(Mono.just(updatedBalanceDao));
        when(userBalanceMapper.userBalanceDaoToUserBalance(updatedBalanceDao)).thenReturn(updatedUserBalance);

        Mono<PaymentResult> result = paymentService.processRefund(testUserId, testAmount, testOrderId);

        StepVerifier.create(result)
                .assertNext(paymentResult -> {
                    assertTrue(paymentResult.isSuccess());
                    assertEquals(transaction, paymentResult.getTransaction());
                    assertEquals(updatedUserBalance, paymentResult.getUpdatedBalance());
                    assertEquals("Оплата успешна", paymentResult.getMessage());
                })
                .verifyComplete();
    }

    @Test
    void processRefund_shouldFailWhenNegativeAmount() {
        BigDecimal negativeAmount = BigDecimal.valueOf(-100.00);
        UUID transactionUuid = UUID.randomUUID();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionUuid(transactionUuid)
                .userUuid(testUserId)
                .orderUuid(testOrderId)
                .amount(negativeAmount)
                .transactionType(TransactionType.REFUND)
                .status(TransactionStatus.FAILED)
                .createdAt(now)
                .build();

        PaymentTransactionDao transactionDao = new PaymentTransactionDao();
        when(paymentTransactionMapper.paymentTransactionToPaymentTransactionDao(any()))
                .thenReturn(transactionDao);
        when(transactionRepository.save(transactionDao))
                .thenReturn(Mono.just(transactionDao));
        when(paymentTransactionMapper.paymentTransactionDaoToPaymentTransaction(transactionDao))
                .thenReturn(transaction);

        UserBalance balance = new UserBalance(UUID.randomUUID(), testUserId, defaultBalance, now, now);
        when(userBalanceRepository.findByUserUuid(testUserId))
                .thenReturn(Mono.just(new UserBalanceDao(balance.getUuid(), testUserId, defaultBalance, now, now)));
        when(userBalanceMapper.userBalanceDaoToUserBalance(any()))
                .thenReturn(balance);

        Mono<PaymentResult> result = paymentService.processRefund(testUserId, negativeAmount, testOrderId);

        StepVerifier.create(result)
                .assertNext(paymentResult -> {
                    assertFalse(paymentResult.isSuccess());
                    assertEquals(TransactionStatus.FAILED, paymentResult.getTransaction().getStatus());
                    assertEquals("Сумма возврата должна быть положительной", paymentResult.getMessage());
                })
                .verifyComplete();

        verify(userBalanceRepository, never()).addToBalance(any(), any());
    }
}