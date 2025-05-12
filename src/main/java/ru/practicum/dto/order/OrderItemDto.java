package ru.practicum.dto.order;

import lombok.*;
import ru.practicum.dto.product.ProductOutDto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO товара заказа
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {
    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Товар
     */
    private ProductOutDto product;

    /**
     * Количество товара в заказе
     */
    private int quantity;

    /**
     * Цена товара в заказе
     */
    private BigDecimal priceAtOrder;
}
