package ru.practicum.model.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Товар
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Название
     */
    @NotBlank
    private String name;

    /**
     * Описание
     */
    @NotBlank
    private String description;

    /**
     * Цена
     */
    @Positive
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
