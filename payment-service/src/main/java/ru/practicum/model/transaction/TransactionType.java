package ru.practicum.model.transaction;

import lombok.Getter;

/**
 * Тип транзакции
 */
@Getter
public enum TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    REFUND
}