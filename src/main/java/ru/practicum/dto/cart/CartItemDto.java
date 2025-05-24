package ru.practicum.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.model.product.Product;

import java.util.UUID;

/**
 * DTO товара корзины
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemDto {

    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Товар
     */
    private Product product;

    /**
     * Количество товара
     */
    private int quantity;
}
