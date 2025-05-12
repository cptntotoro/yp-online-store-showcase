package ru.practicum.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CartItem {
    private Long productId;
    private String productName;
    private String imageUrl;
    private double price;
    private int quantity;

    public double getTotalPrice() {
        return price * quantity;
    }
}
