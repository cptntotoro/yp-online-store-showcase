package ru.practicum.dto.order;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import ru.practicum.model.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO заказа
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Статус заказа
     */
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    /**
     * Стоимость заказа
     */
    private BigDecimal totalPrice;

    /**
     * Товары заказа
     */
    private List<OrderItemDto> items = new ArrayList<>();

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;
}
