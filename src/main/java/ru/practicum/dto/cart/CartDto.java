package ru.practicum.dto.cart;

import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO корзины
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Список DTO товаров корзины
     */
    private List<CartItemDto> items = new ArrayList<>();
}
