package ru.practicum.model.cart;

import lombok.*;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Корзина товаров
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {
    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Идентификатор пользователя
     */
    private UUID userUuid;

    /**
     * Товары корзины
     */
    private Flux<CartItem> items;

    /**
     * Стоимость корзины товаров
     */
    private BigDecimal totalPrice;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    private LocalDateTime updatedAt;
}
