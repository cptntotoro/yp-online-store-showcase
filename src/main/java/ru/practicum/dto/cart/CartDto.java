package ru.practicum.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO корзины
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    /**
     * Идентификатор
     */
    private UUID uuid;
//
//    /**
//     * Идентификатор пользователя
//     */
//    private UUID userUuid;

    /**
     * Список DTO товаров корзины
     */
    private List<CartItemDto> items = new ArrayList<>();
}
