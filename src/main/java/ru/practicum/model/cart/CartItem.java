package ru.practicum.model.cart;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.model.product.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Товар корзины
 */
@Entity
@Table(name = "cart_items")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "cart_item_uuid", updatable = false, nullable = false)
    private UUID uuid;

    /**
     * Корзина товаров
     */
    @ManyToOne
    @JoinColumn(name = "cart_uuid", nullable = false)
    private Cart cart;

    /**
     * Товар
     */
    @ManyToOne
    @JoinColumn(name = "product_uuid", nullable = false)
    private Product product;

    /**
     * Количество товаров
     */
    private int quantity;

    /**
     * Дата создания
     */
    @CreationTimestamp
    private LocalDateTime createdAt;

    public CartItem(Cart cart, Product product, int quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    /**
     * Получить стоимость товара корзины
     *
     * @return Стоимость товара корзины
     */
    @Transient
    public BigDecimal getTotalPrice() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
