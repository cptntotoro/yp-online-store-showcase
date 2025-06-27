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
                .map(userBalanceMapper::userBalanceDaoToUserBalance)
                .defaultIfEmpty(createDefaultBalance(userUuid));
    }

    @Transactional
    @Override
    public Mono<PaymentResult> processPayment(UUID userUuid, BigDecimal amount, UUID orderId) {
        return getUserBalance(userUuid)
                .flatMap(balance -> {
                    if (balance.getAmount().compareTo(amount) < 0) {
                        return createAndSaveTransaction(userUuid, amount, orderId, TransactionStatus.FAILED, TransactionType.WITHDRAWAL)
                                .map(transaction ->
                                        PaymentResult.failed(
                                                transaction,
                                                balance,
                                                "Недостаточно средств на счете"
                                        )
                                );
                    }

                    return userBalanceRepository.deductBalance(userUuid, amount)
                            .map(userBalanceMapper::userBalanceDaoToUserBalance)
                            .flatMap(updatedBalance ->
                                    createAndSaveTransaction(userUuid, amount, orderId, TransactionStatus.COMPLETED, TransactionType.WITHDRAWAL)
                                            .map(transaction -> PaymentResult.success(transaction, updatedBalance))
                            );
                });
    }

    @Override
    @Transactional
    public Mono<PaymentResult> processRefund(UUID userUuid, BigDecimal amount, UUID orderId) {
        return userBalanceRepository.addToBalance(userUuid, amount)
                .map(userBalanceMapper::userBalanceDaoToUserBalance)
                .flatMap(updatedBalance ->
                        createAndSaveTransaction(userUuid, amount, orderId, TransactionStatus.COMPLETED, TransactionType.REFUND)
                                .map(transaction -> PaymentResult.success(transaction, updatedBalance))
                )
                .defaultIfEmpty(PaymentResult.failed(
                        null,
                        null,
                        "Ошибка при возврате средств"
                ));
    }

    private UserBalance createDefaultBalance(UUID userId) {
        return UserBalance.builder()
                .userUuid(userId)
                .amount(defaultBalance)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private Mono<PaymentTransaction> createAndSaveTransaction(UUID userId, BigDecimal amount, UUID orderId,
            TransactionStatus status, TransactionType type) {
        PaymentTransaction transaction = PaymentTransaction.builder()
                .transactionUuid(UUID.randomUUID())
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