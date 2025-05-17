package ru.practicum.service.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.product.ProductNotFoundException;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.product.Product;
import ru.practicum.repository.cart.CartRepository;
import ru.practicum.repository.product.ProductRepository;

import java.math.BigDecimal;
import java.util.*;

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
                .orElseGet(() -> create(userUuid));
    }

    @Override
    @CacheEvict(value = "cartTotals", key = "#userUuid")
    public Cart addToCart(UUID userUuid, UUID productUuid, int quantity) {
        Cart cart = get(userUuid);

        Product product = productRepository.findById(productUuid)
                .orElseThrow(() -> new ProductNotFoundException("Товар с uuid " + productUuid + " не найден."));

        cart.addItem(product, quantity);
        return cartRepository.save(cart);
    }

    @Override
    @CacheEvict(value = "cartTotals", key = "#userUuid")
    public Cart removeFromCart(UUID userUuid, UUID productUuid) {
        Cart cart = get(userUuid);
        cart.removeItem(productUuid);
        return cartRepository.save(cart);
    }

//    @Override
//    public void updateQuantity(UUID productUuid, int quantity) {
//        if (quantity <= 0) {
//            remove(productUuid);
//        } else {
//            CartItem item = cart.get(productUuid);
//            if (item != null) {
//                item.setQuantity(quantity);
//            }
//        }
//    }
//
//    @Override
//    public List<CartItem> getAll() {
//        return new ArrayList<>(cart.values());
//    }

    @Override
    @CacheEvict(value = "cartTotals", key = "#userUuid")
    public void clear(UUID userUuid) {
        Cart cart = get(userUuid);
        cart.clear();
        cartRepository.save(cart);
    }

    @Cacheable(value = "cartTotals", key = "#userUuid")
    @Override
    public BigDecimal getCachedCartTotal(UUID userUuid) {
        return calculateCartTotal(userUuid);
    }

    private BigDecimal calculateCartTotal(UUID userUuid) {
        Cart cart = get(userUuid);
        return cart.getItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

//    @Override
//    public int getTotalItems() {
//        return cart.values().stream()
//                .mapToInt(CartItem::getQuantity)
//                .sum();
//    }
//
//    @Override
//    public BigDecimal getTotalPrice() {
//        return cart.values().stream()
//                .map(CartItem::getTotalPrice)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//    }
}