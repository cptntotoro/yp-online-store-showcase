package ru.practicum.model.order;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import ru.practicum.model.product.Product;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Товар заказа
 */
@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "order_item_uuid", updatable = false, nullable = false)
    private UUID uuid;

    /**
     * Заказ
     */
    @ManyToOne
    @JoinColumn(name = "order_uuid", nullable = false)
    private Order order;

    /**
     * Товар
     */
    @ManyToOne
    @JoinColumn(name = "product_uuid", nullable = false)
    private Product product;

    /**
     * Количество товара в заказе
     */
    @Positive
    private int quantity;

    /**
     * Цена товара в заказе
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal priceAtOrder;

    public OrderItem(Order order, Product product, int quantity, BigDecimal priceAtOrder) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
    }

    /**
     * Получить стоимость товара в заказе
     * @return Стоимость товара в заказе
     */
    @Transient
    public BigDecimal getTotalPrice() {
        return priceAtOrder.multiply(BigDecimal.valueOf(quantity));
    }
}
