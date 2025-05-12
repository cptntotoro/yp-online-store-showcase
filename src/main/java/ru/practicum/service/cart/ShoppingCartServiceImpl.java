package ru.practicum.service.cart;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import ru.practicum.model.CartItem;
import ru.practicum.model.Product;

import java.util.*;

@Service
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final Map<UUID, CartItem> cart = new HashMap<>();

    @Override
    public void add(Product product, int quantity) {
        CartItem existingItem = cart.get(product.getUuid());

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            cart.put(product.getUuid(), new CartItem(
                    product.getUuid(),
                    product.getName(),
                    product.getImageUrl(),
                    product.getPrice(),
                    quantity
            ));
        }
    }

    @Override
    public void remove(UUID productUuid) {
        cart.remove(productUuid);
    }

    @Override
    public void updateQuantity(UUID productUuid, int quantity) {
        if (quantity <= 0) {
            remove(productUuid);
        } else {
            CartItem item = cart.get(productUuid);
            if (item != null) {
                item.setQuantity(quantity);
            }
        }
    }

    @Override
    public List<CartItem> getAll() {
        return new ArrayList<>(cart.values());
    }

    @Override
    public void clear() {
        cart.clear();
    }

    @Override
    public int getTotalItems() {
        return cart.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    @Override
    public double getTotalPrice() {
        return cart.values().stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }
}