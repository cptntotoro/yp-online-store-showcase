package ru.practicum.dto.cart;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO корзины
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {
    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Список DTO товаров корзины
     */
    @Builder.Default
    private List<CartItemDto> items = new ArrayList<>();

    /**
     * Стоимость корзины
     */
    private BigDecimal totalPrice;
}
