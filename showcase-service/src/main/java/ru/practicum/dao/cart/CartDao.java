package ru.practicum.dao.cart;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO корзины товаров
 */
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDao {
    /**
     * Идентификатор
     */
    @Id
    @Column("cart_uuid")
    private UUID uuid;

    /**
     * Идентификатор пользователя
     */
    @Column("user_uuid")
    private UUID userUuid;

    /**
     * Стоимость корзины товаров
     */
    @Column("total_price")
    private BigDecimal totalPrice;

    /**
     * Дата создания
     */
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
