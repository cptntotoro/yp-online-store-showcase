package ru.practicum.dto.product;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO кеша товаров
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @JsonCreator)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductCacheDto {

    /**
     * Идентификатор
     */
    @JsonProperty("uuid")
    private UUID uuid;

    /**
     * Название
     */
    @JsonProperty("name")
    private String name;

    /**
     * Описание
     */
    @JsonProperty("description")
    private String description;

    /**
     * Цена
     */
    @JsonProperty("price")
    private BigDecimal price;

    /**
     * Ссылка на изображение
     */
    @JsonProperty("imageUrl")
    private String imageUrl;
}
