package ru.practicum.dto.product;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Исходящее DTO товара
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOutDto {
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
