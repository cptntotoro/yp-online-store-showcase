package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private UUID productUuid;
    private String productName;
    private String imageUrl;
    private double price;
    private int quantity;

    public double getTotalPrice() {
        return price * quantity;
    }
}
