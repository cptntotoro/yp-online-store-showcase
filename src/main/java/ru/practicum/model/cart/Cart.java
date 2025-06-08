package ru.practicum.model.cart;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

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
