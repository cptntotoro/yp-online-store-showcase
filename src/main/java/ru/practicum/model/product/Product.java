package ru.practicum.model.product;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Товар
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "product_uuid", updatable = false, nullable = false)
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
    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Дата создания
     */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Ссылка на изображение
     */
    private String imageUrl;
}
