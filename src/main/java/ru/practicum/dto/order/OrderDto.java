package ru.practicum.dto.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.practicum.model.order.OrderStatus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO заказа
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {

    /**
     * Идентификатор
     */
    private UUID uuid;

//    /**
//     * Идентификатор пользователя
//     */
//    private UUID userUuid;

    /**
     * Статус заказа
     */
    private OrderStatus status;

    /**
     * Стоимость заказа
     */
    private BigDecimal totalAmount;

    /**
     * Товары заказа
     */
    private List<OrderItemDto> items = new ArrayList<>();
}
