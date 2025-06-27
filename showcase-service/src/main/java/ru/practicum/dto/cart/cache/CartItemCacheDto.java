package ru.practicum.dto.cart.cache;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO товара корзины для кеша
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @JsonCreator)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartItemCacheDto {

    /**
     * Идентификатор
     */
    @JsonProperty("uuid")
    private UUID uuid;

    /**
     * Идентификатор корзины
     */
    @JsonProperty("cartUuid")
    private UUID cartUuid;

    /**
     * Количество товаров
     */
    @JsonProperty("quantity")
    private int quantity;

    /**
     * Идентификатор товара
     */
    @JsonProperty("productUuid")
    private UUID productUuid;
}