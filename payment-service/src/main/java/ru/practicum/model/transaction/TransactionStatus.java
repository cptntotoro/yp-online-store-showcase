package ru.practicum.model.transaction;

import lombok.Getter;

/**
 * Статус транзакции
 */
@Getter
public enum TransactionStatus {
    PENDING("Обработка"),
    COMPLETED("Завершен"),
    FAILED("Ошибка");

    /**
     * Название статуса транзакции
     */
    private final String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }
}