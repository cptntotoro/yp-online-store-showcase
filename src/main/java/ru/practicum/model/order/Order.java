package ru.practicum.model.order;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.model.cart.Cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Заказ
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "order_uuid", updatable = false, nullable = false)
    private UUID uuid;

    /**
     * Идентификатор пользователя
     */
    @Column(name = "user_uuid", nullable = false)
    private UUID userUuid;

    /**
     * Корзина
     */
    @OneToOne
    @JoinColumn(name = "cart_uuid", nullable = false)
    private Cart cart;

    /**
     * Статус заказа
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    /**
     * Стоимость заказа
     */
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalPrice;

    /**
     * Товары заказа
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Дата создания
     */
    @CreationTimestamp
    private LocalDateTime createdAt;

    public Order(UUID userUuid, Cart cart) {
        this.userUuid = userUuid;
        this.cart = cart;
        this.status = OrderStatus.CREATED;
        this.totalPrice = cart.getTotalPrice();

        // Переносим элементы из корзины в заказ
        cart.getItems().forEach(cartItem -> {
            OrderItem orderItem = new OrderItem(
                    this,
                    cartItem.getProduct(),
                    cartItem.getQuantity(),
                    cartItem.getProduct().getPrice()
            );
            items.add(orderItem);
        });
    }
}
