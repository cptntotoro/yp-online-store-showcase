package ru.practicum.model.transaction;

import lombok.Getter;

/**
 * Статус транзакции
 */
@Getter
public enum TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED
}