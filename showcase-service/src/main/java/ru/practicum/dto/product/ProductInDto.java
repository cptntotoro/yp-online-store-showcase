package ru.practicum.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Входящее DTO товара
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductInDto {
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
