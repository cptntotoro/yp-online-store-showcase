package ru.practicum.model.payment;

import lombok.*;
import ru.practicum.model.balance.UserBalance;
import ru.practicum.model.transaction.PaymentTransaction;

/**
 * Результат оплаты
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResult {
    /**
     * DAO платежной транзакции
     */
    private PaymentTransaction transaction;
    /**
     * Баланс счета пользователя
     */
    private UserBalance updatedBalance;
    /**
     * Сообщение
     */
    private String message;
    /**
     * Флаг успеха
     */
    private boolean isSuccess;

    public static PaymentResult successfulPaymentResult(PaymentTransaction transaction, UserBalance updatedBalance) {
        return PaymentResult.builder()
                .transaction(transaction)
                .updatedBalance(updatedBalance)
                .message("Оплата успешна")
                .isSuccess(true)
                .build();
    }

    public static PaymentResult failedPaymentResult(PaymentTransaction transaction, UserBalance balance, String message) {
        return PaymentResult.builder()
                .transaction(transaction)
                .updatedBalance(balance)
                .message(message)
                .isSuccess(false)
                .build();
    }
}