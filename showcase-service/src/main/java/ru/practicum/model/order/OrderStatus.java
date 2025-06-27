package ru.practicum.model.order;

import lombok.Getter;
import ru.practicum.exception.order.IllegalOrderStateException;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Статус заказа
 */
@Getter
public enum OrderStatus {
    CREATED("Создан"),
    PAID("Оплачен"),
    DELIVERED("Доставлен"),
    CANCELLED("Отменен");

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITION_RULES = Map.of(
            CREATED, EnumSet.of(PAID, CANCELLED),
            PAID, EnumSet.of(DELIVERED, CANCELLED),
            DELIVERED, EnumSet.noneOf(OrderStatus.class),
            CANCELLED, EnumSet.noneOf(OrderStatus.class)
    );

    /**
     * Название статуса заказа
     */
    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Проверить допустимость перехода между статусами
     *
     * @param newStatus Новый статус
     * @throws IllegalOrderStateException Если переход недопустим
     */
    public void validateTransition(OrderStatus newStatus) {
        Set<OrderStatus> allowedTransitions = TRANSITION_RULES.get(this);
        if (allowedTransitions == null || !allowedTransitions.contains(newStatus)) {
            throw new IllegalOrderStateException(
                    String.format("Недопустимый переход статуса заказа из %s в %s", this, newStatus)
            );
        }
    }
}