package ru.practicum.model.order;

import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Товар заказа
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Заказ
     */
    private UUID orderUuid;

    /**
     * Товар
     */
    private UUID productUuid;

    /**
     * Количество товара в заказе
     */
    @Positive
    private int quantity;

    /**
     * Цена товара в заказе
     */
    private BigDecimal priceAtOrder;
}
