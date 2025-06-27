package ru.practicum.dto.cart.cache;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * DTO корзины для кеша
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @JsonCreator)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartCacheDto {

    /**
     * Идентификатор
     */
    @JsonProperty("uuid")
    private UUID uuid;

    /**
     * Идентификатор пользователя
     */
    @JsonProperty("userUuid")
    private UUID userUuid;

    /**
     * Товары заказа
     */
    @JsonProperty("items")
    private List<CartItemCacheDto> items;

    /**
     * Стоимость заказа
     */
    @JsonProperty("totalPrice")
    private BigDecimal totalPrice;
}