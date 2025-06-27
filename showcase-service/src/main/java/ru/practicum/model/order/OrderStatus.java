package ru.practicum.model.order;

import lombok.Getter;

/**
 * Статус заказа
 */
@Getter
public enum OrderStatus {
    CREATED("Создан"),
    PAID("Оплачен"),
    DELIVERED("Доставлен"),
    CANCELLED("Отменен");

    /**
     * Название статуса заказа
     */
    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }
}