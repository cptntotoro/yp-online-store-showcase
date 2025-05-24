package ru.practicum.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.dto.product.ProductDto;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO товара заказа
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDto {

    /**
     * Идентификатор
     */
    private UUID uuid;

//    /**
//     * Заказ
//     */
//    private Order order;

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
