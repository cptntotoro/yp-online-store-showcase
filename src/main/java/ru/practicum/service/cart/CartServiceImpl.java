package ru.practicum.service.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.cart.CartNotFoundException;
import ru.practicum.exception.product.ProductNotFoundException;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.product.Product;
import ru.practicum.repository.cart.CartRepository;
import ru.practicum.repository.product.ProductRepository;

import java.math.BigDecimal;
import java.util.*;

/**
 * Сервис управления корзиной товаров
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    @Override
    public Cart create(UUID userUuid) {
        Cart newCart = new Cart();
        newCart.setUserUuid(userUuid);
        return cartRepository.save(newCart);
    }

    @Override
    public Cart get(UUID userUuid) {
        return cartRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new CartNotFoundException("Корзина пользователя с uuid = " + userUuid + " не найдена."));
    }

    @Override
    @CacheEvict(value = "cartTotals", key = "#userUuid")
    public Cart addToCart(UUID userUuid, UUID productUuid, int quantity) {
        Cart cart = get(userUuid);

        Product product = productRepository.findById(productUuid)
                .orElseThrow(() -> new ProductNotFoundException("Товар с uuid " + productUuid + " не найден."));

        addItem(cart, product, quantity);
        return cartRepository.save(cart);
    }

    @Override
    @CacheEvict(value = "cartTotals", key = "#userUuid")
    public Cart removeFromCart(UUID userUuid, UUID productUuid) {
        Cart cart = get(userUuid);
        removeItem(cart, productUuid);
        return cartRepository.save(cart);
    }

    @Override
    @CacheEvict(value = "cartTotals", key = "#userUuid")
    public void clear(UUID userUuid) {
        Cart cart = get(userUuid);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    @Override
    @Cacheable(value = "cartTotals", key = "#userUuid")
    public BigDecimal getCachedCartTotal(UUID userUuid) {
        Cart cart = get(userUuid);
        return cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @CacheEvict(value = "cartTotals", key = "#userUuid")
    public void updateQuantity(UUID userUuid, UUID productUuid, int quantity) {
        Cart cart = get(userUuid);
        cart.getItems().stream()
                .filter(item -> item.getProduct().getUuid().equals(productUuid))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
        cartRepository.save(cart);
    }

    public void addItem(Cart cart, Product product, int quantity) {
        List<CartItem> cartItems = cart.getItems();

        Optional<CartItem> existingItem = cartItems.stream()
                .filter(item -> item.getProduct().getUuid().equals(product.getUuid()))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem(cart, product, quantity);
            cartItems.add(newItem);
        }
    }

    public void removeItem(Cart cart, UUID productUuid) {
        cart.getItems().removeIf(item -> item.getProduct().getUuid().equals(productUuid));
    }
}