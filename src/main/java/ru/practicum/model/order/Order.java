package ru.practicum.model.order;

import lombok.*;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Заказ
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Идентификатор пользователя
     */
    private UUID userUuid;

    private UUID cartUuid;

    /**
     * Статус заказа
     */
    private OrderStatus status;

    /**
     * Стоимость заказа
     */
    private BigDecimal totalPrice;

    /**
     * Товары заказа
     */
    private Flux<OrderItem> items;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;
}
