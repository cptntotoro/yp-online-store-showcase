package ru.practicum.dao.product;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("products")
public class ProductDao {
    @Id
    @Column("product_uuid")
    private UUID uuid;

    private String name;

    private String description;

    private BigDecimal price;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("image_url")
    private String imageUrl;
}
