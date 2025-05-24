package ru.practicum.dto.cart;

import lombok.*;
import ru.practicum.dto.product.ProductDto;

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
    private ProductDto product;

    /**
     * Количество товара
     */
    private int quantity;
}
