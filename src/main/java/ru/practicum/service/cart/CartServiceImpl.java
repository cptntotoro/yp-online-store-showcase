package ru.practicum.service.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.practicum.exception.cart.IllegalCartStateException;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.product.Product;
import ru.practicum.repository.cart.CartRepository;
import ru.practicum.service.product.ProductService;

import java.math.BigDecimal;
import java.util.*;

/**
 * Сервис управления корзиной товаров
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    /**
     * Репозиторий корзины товаров
     */
    private final CartRepository cartRepository;

    /**
     * Сервис управления товарами
     */
    private final ProductService productService;

    private final CartCacheService cartCacheService;

    @Override
    public Mono<Cart> createGuest(UUID userUuid) {
        Cart newCart = new Cart();
        newCart.setUserUuid(userUuid);
        newCart.setTotalPrice(BigDecimal.ZERO);
        return cartRepository.save(newCart);
    }

    @Override
    public Mono<Cart> get(UUID userUuid) {
        return cartCacheService.getCart(userUuid);
    }

    @Override
    public Mono<Cart> addToCart(UUID userUuid, UUID productUuid, int quantity) {
        if (quantity <= 0) {
            return Mono.error(new IllegalCartStateException("Количество товара не может быть меньше или равно нулю"));
        }

        return Mono.zip(get(userUuid), productService.getByUuid(productUuid))
                .flatMap(tuple -> {
                    Cart cart = tuple.getT1();
                    Product product = tuple.getT2();
                    updateOrAddItem(cart, product, quantity);
                    updateCartTotal(cart);
                    return cartRepository.save(cart);
                })
                .doOnSuccess(saved -> cartCacheService.evict(userUuid));
    }

    @Override
    public Mono<Cart> removeFromCart(UUID userUuid, UUID productUuid) {
        return get(userUuid)
                .flatMap(cart -> {
                    removeCartItem(cart, productUuid);
                    updateCartTotal(cart);
                    return cartRepository.save(cart);
                })
                .doOnSuccess(saved -> cartCacheService.evict(userUuid));
    }

    @Override
    public Mono<Void> clear(UUID userUuid) {
        return get(userUuid)
                .flatMap(cart -> {
                    cart.getItems().clear();
                    updateCartTotal(cart);
                    return cartRepository.save(cart).then();
                })
                .doOnSuccess(saved -> cartCacheService.evict(userUuid));
    }

    @Override
    public Mono<Cart> updateQuantity(UUID userUuid, UUID productUuid, int quantity) {
        if (quantity <= 0) {
            return Mono.error(new IllegalCartStateException("Количество товара не может быть меньше или равно нулю"));
        }

        return get(userUuid)
                .flatMap(cart -> {
                    updateItemQuantity(cart, productUuid, quantity);
                    updateCartTotal(cart);
                    return cartRepository.save(cart);
                })
                .doOnSuccess(unused -> cartCacheService.evict(userUuid));
    }

    /**
     * Изменить наличие товара в корзине или добавить новый товар
     *
     * @param cart     Корзина
     * @param product  Товар
     * @param quantity Количество товара
     */
    private void updateOrAddItem(Cart cart, Product product, int quantity) {
        cart.getItems().stream()
                .filter(item -> item.getProduct().equals(product))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + quantity),
                        () -> cart.getItems().add(new CartItem(cart, product, quantity))
                );
    }

    /**
     * Удалить товар из корзины
     *
     * @param cart        Корзина
     * @param productUuid Идентификатор товара
     */
    private void removeCartItem(Cart cart, UUID productUuid) {
        cart.getItems().removeIf(item -> item.getProduct().getUuid().equals(productUuid));
    }

    /**
     * Обновить количество товара
     *
     * @param cart        Корзина
     * @param productUuid Идентификатор товара
     * @param quantity    Количество товара
     */
    private void updateItemQuantity(Cart cart, UUID productUuid, int quantity) {
        cart.getItems().stream()
                .filter(item -> item.getProduct().getUuid().equals(productUuid))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
    }

    /**
     * Установить новое значение стоимости товаров в корзине
     *
     * @param cart Корзина
     */
    private void updateCartTotal(Cart cart) {
        cart.setTotalPrice(calculateTotalPrice(cart.getItems()));
    }

    /**
     * Рассчитать стоимость товаров в корзине
     *
     * @param items Товары
     * @return Стоимость товаров в корзине
     */
    private BigDecimal calculateTotalPrice(List<CartItem> items) {
        return items.stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}