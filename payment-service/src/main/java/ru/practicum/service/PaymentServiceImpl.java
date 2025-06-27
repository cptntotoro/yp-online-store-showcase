package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.practicum.dao.transaction.PaymentTransactionDao;
import ru.practicum.mapper.transaction.PaymentTransactionMapper;
import ru.practicum.mapper.balance.UserBalanceMapper;
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

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    /**
     * Репозиторий баланса счета пользователя
     */
    private final UserBalanceRepository userBalanceRepository;

    /**
     * Репозиторий транзакций
     */
    private final TransactionRepository transactionRepository;

    /**
     * Маппер баланса счета пользователя
     */
    private final UserBalanceMapper userBalanceMapper;

    /**
     * Маппер транзакции оплаты
     */
    private final PaymentTransactionMapper paymentTransactionMapper;

    @Value("${payment.default-balance}")
    public BigDecimal defaultBalance;

    @Override
    public Mono<UserBalance> getUserBalance(UUID userUuid) {
        return userBalanceRepository.findByUserUuid(userUuid)
                .switchIfEmpty(Mono.defer(() -> userBalanceRepository.create(userUuid, defaultBalance)))
                .map(userBalanceMapper::userBalanceDaoToUserBalance);
    }

    @Override
    @Transactional
    public Mono<PaymentResult> processPayment(UUID userUuid, BigDecimal amount, UUID orderId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return createFailedTransaction(userUuid, amount, orderId,
                    "Сумма платежа должна быть положительной");
        }

        return ensureBalanceExists(userUuid)
                .flatMap(balance -> {
                    if (balance.getAmount().compareTo(amount) < 0) {
                        return createFailedTransaction(userUuid, amount, orderId,
                                "Недостаточно средств на счете");
                    }

                    return userBalanceRepository.deductBalance(userUuid, amount)
                            .map(userBalanceMapper::userBalanceDaoToUserBalance)
                            .flatMap(updatedBalance ->
                                    createAndSaveTransaction(userUuid, amount, orderId,
                                            TransactionStatus.COMPLETED, TransactionType.WITHDRAWAL)
                                            .map(transaction ->
                                                    PaymentResult.successfulPaymentResult(transaction, updatedBalance))
                            );
                });
    }

    @Override
    @Transactional
    public Mono<PaymentResult> processRefund(UUID userUuid, BigDecimal amount, UUID orderId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return createFailedTransaction(userUuid, amount, orderId,
                    "Сумма возврата должна быть положительной");
        }

        return ensureBalanceExists(userUuid)
                .flatMap(__ -> userBalanceRepository.addToBalance(userUuid, amount))
                .map(userBalanceMapper::userBalanceDaoToUserBalance)
                .flatMap(updatedBalance ->
                        createAndSaveTransaction(userUuid, amount, orderId,
                                TransactionStatus.COMPLETED, TransactionType.REFUND)
                                .map(transaction ->
                                        PaymentResult.successfulPaymentResult(transaction, updatedBalance))
                );
    }

    private Mono<PaymentResult> createFailedTransaction(UUID userUuid, BigDecimal amount, UUID orderId,
                                                        String errorMessage) {
        return createAndSaveTransaction(userUuid, amount, orderId,
                TransactionStatus.FAILED, TransactionType.WITHDRAWAL)
                .flatMap(transaction ->
                        userBalanceRepository.findByUserUuid(userUuid)
                                .switchIfEmpty(Mono.defer(() -> userBalanceRepository.create(userUuid, defaultBalance)))
                                .map(userBalanceMapper::userBalanceDaoToUserBalance)
                                .map(balance ->
                                        PaymentResult.failedPaymentResult(transaction, balance, errorMessage)));
    }

    private Mono<UserBalance> ensureBalanceExists(UUID userUuid) {
        return userBalanceRepository.findByUserUuid(userUuid)
                .switchIfEmpty(Mono.defer(() -> userBalanceRepository.create(userUuid, defaultBalance)))
                .map(userBalanceMapper::userBalanceDaoToUserBalance);
    }

    private Mono<PaymentTransaction> createAndSaveTransaction(UUID userId, BigDecimal amount, UUID orderId,
            TransactionStatus status, TransactionType type) {
        PaymentTransaction transaction = PaymentTransaction.builder()
                .userUuid(userId)
                .orderUuid(orderId)
                .amount(amount)
                .transactionType(type)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();

        PaymentTransactionDao dao = paymentTransactionMapper
                .paymentTransactionToPaymentTransactionDao(transaction);

        return transactionRepository.save(dao)
                .map(paymentTransactionMapper::paymentTransactionDaoToPaymentTransaction);
    }
}