package ru.practicum.model.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Товар
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product {

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
     * Дата создания
     */
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    private LocalDateTime updatedAt;

    /**
     * Ссылка на изображение
     */
    private String imageUrl;
}
