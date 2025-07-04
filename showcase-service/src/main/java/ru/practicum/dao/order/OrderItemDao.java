package ru.practicum.dao.order;

import jakarta.validation.constraints.Positive;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DAO товара заказа
 */
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDao {

    /**
     * Идентификатор
     */
    @Id
    @Column("order_item_uuid")
    private UUID uuid;

    /**
     * Заказ
     */
    @Column("order_uuid")
    private UUID orderUuid;

    /**
     * Товар
     */
    @Column("product_uuid")
    private UUID productUuid;

    /**
     * Количество товара в заказе
     */
    @Positive
    private int quantity;

    /**
     * Цена товара в заказе
     */
    @Column("price_at_order")
    private BigDecimal priceAtOrder;
}
