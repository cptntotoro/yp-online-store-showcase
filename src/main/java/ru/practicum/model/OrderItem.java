package ru.practicum.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
    @Id
    @Column(name = "order_item_uuid")
    private UUID uuid;

    @ManyToOne
    @JoinColumn(name = "product_uuid")
    private Product product;

    @Positive
    private int quantity;

    @ManyToOne
    @JoinColumn(name = "order_uuid")
    private Order order;

    @Transient
    public Double getTotalPrice() {
        return product.getPrice() * quantity;
    }
}
