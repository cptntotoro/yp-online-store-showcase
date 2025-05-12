package ru.practicum.model.cart;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Корзина товаров
 */
@Entity
@Table(name = "carts")
@DynamicInsert
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "cart_uuid", updatable = false, nullable = false)
    private UUID uuid;

    /**
     * Идентификатор пользователя
     */
    @Column(name = "user_uuid", nullable = false)
    private UUID userUuid;

    /**
     * Товары корзины
     */
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

    /**
     * Стоимость корзины товаров
     */
    @Column(name = "total_price")
    private BigDecimal totalPrice;

    /**
     * Дата создания
     */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
