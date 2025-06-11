package ru.practicum.dao.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.practicum.model.order.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Заказ
 */
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDao {

    /**
     * Идентификатор
     */
    @Id
    @Column("order_uuid")
    private UUID uuid;

    /**
     * Идентификатор пользователя
     */
    @Column("user_uuid")
    private UUID userUuid;

    /**
     * Идентификатор корзины
     */
    @Column("cart_uuid")
    private UUID cartUuid;

    /**
     * Статус заказа
     */
    private OrderStatus status;

    /**
     * Стоимость заказа
     */
    @Column("total_amount")
    private BigDecimal totalPrice;

    /**
     * Дата создания
     */
    @Column("created_at")
    private LocalDateTime createdAt;

//    public Order(UUID userUuid, Cart cart) {
//        this.userUuid = userUuid;
//        this.cart = cart;
//        this.status = OrderStatus.CREATED;
//        this.totalPrice = cart.getTotalPrice();
//
//        // Переносим элементы из корзины в заказ
//        cart.getItems().forEach(cartItem -> {
//            OrderItem orderItem = new OrderItem(
//                    this,
//                    cartItem.getProduct(),
//                    cartItem.getQuantity(),
//                    cartItem.getProduct().getPrice()
//            );
//            items.add(orderItem);
//        });
//    }
}
