package ru.practicum.dto.cart;

import lombok.*;
import ru.practicum.dto.product.ProductOutDto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO товара корзины
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Товар
     */
    private ProductOutDto product;

    /**
     * Количество товара
     */
    private int quantity;

    /**
     * Стоимость корзины
     */
    private BigDecimal totalPrice;
}
