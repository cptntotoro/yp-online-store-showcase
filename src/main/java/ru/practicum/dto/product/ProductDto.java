package ru.practicum.dto.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO товара
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Название
     */
    private String name;

    /**
     * Описание
     */
    private String description;

    /**
     * Цена
     */
    private BigDecimal price;

    /**
     * Ссылка на изображение
     */
    private String imageUrl;
}
