package ru.practicum.model.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.model.product.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Заказы пользоввателя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdersWithTotal {
    /**
     * Список заказов
     */
    private List<Order> orders;

    /**
     * Товары заказов
     */
    private Map<UUID, Product> products;

    /**
     * Стоимость заказов
     */
    private BigDecimal totalAmount;
}
