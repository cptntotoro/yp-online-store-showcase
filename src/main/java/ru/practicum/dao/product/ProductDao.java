package ru.practicum.dao.product;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO товара
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("products")
public class ProductDao {
    /**
     * Идентификатор
     */
    @Id
    @Column("product_uuid")
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
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    @Column("updated_at")
    private LocalDateTime updatedAt;

    /**
     * Ссылка на изображение
     */
    @Column("image_url")
    private String imageUrl;
}
