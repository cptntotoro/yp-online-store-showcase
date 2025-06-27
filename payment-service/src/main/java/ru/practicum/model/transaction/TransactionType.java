package ru.practicum.model.transaction;

import lombok.Getter;

/**
 * Тип транзакции
 */
@Getter
public enum TransactionType {
    DEPOSIT("Пополнение"),
    WITHDRAWAL("Снятие"),
    REFUND("Возврат");

    /**
     * Название типа транзакции
     */
    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }
}