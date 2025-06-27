package ru.practicum.model.cart;

import lombok.*;
import org.springframework.data.annotation.Transient;
import ru.practicum.model.product.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Товар корзины
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CartItem {

    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Идентификатор корзины
     */
    private UUID cartUuid;

    /**
     * Товар
     */
    private Product product;

    /**
     * Количество товаров
     */
    private int quantity;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;

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
