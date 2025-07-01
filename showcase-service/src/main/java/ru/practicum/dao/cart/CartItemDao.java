package ru.practicum.dao.cart;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO товара корзины
 */
@Table(name = "cart_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItemDao {

    /**
     * Идентификатор
     */
    @Id
    @Column("cart_item_uuid")
    private UUID uuid;

    /**
     * Идентификатор корзины
     */
    @Column("cart_uuid")
    private UUID cartUuid;

    /**
     * Товар
     */
    @Column("product_uuid")
    private UUID productUuid;

    /**
     * Количество товаров
     */
    private int quantity;

    /**
     * Дата создания
     */
    @Column("created_at")
    private LocalDateTime createdAt;
}
