package ru.practicum.service.cart;

import ru.practicum.model.CartItem;
import ru.practicum.model.Product;

import java.util.List;
import java.util.UUID;

/**
 * Сервис управления корзиной товаров
 */
public interface ShoppingCartService {
    void add(Product product, int quantity);

    void remove(UUID productUuid);

    void updateQuantity(UUID productUuid, int quantity);

    List<CartItem> getAll();

    void clear();

    int getTotalItems();

    double getTotalPrice();
}
