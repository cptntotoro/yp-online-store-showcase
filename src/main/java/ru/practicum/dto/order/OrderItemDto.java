package ru.practicum.dto.order;

import lombok.*;
import ru.practicum.dto.product.ProductDto;

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
    private ProductDto product;

    /**
     * Количество товара в заказе
     */
    private int quantity;

    // TODO:
    private BigDecimal priceAtOrder;
}
