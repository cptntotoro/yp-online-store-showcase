package ru.practicum.service;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.model.CartItem;
import ru.practicum.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShoppingCartService {
    private final Map<Long, CartItem> cart = new HashMap<>();

    public void addToCart(Product product, int quantity) {
        CartItem existingItem = cart.get(product.getId());

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            cart.put(product.getId(), new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getImageUrl(),
                    product.getPrice(),
                    quantity
            ));
        }
    }

    public void removeFromCart(Long productId) {
        cart.remove(productId);
    }

    public void updateQuantity(Long productId, int quantity) {
        if (quantity <= 0) {
            removeFromCart(productId);
        } else {
            CartItem item = cart.get(productId);
            if (item != null) {
                item.setQuantity(quantity);
            }
        }
    }

    public List<CartItem> getCartItems() {
        return new ArrayList<>(cart.values());
    }

    public void clearCart() {
        cart.clear();
    }

    public int getTotalItems() {
        return cart.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public double getTotalPrice() {
        return cart.values().stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }
}