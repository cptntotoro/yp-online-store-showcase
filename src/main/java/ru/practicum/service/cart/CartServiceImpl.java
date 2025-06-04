package ru.practicum.service.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.cart.CartNotFoundException;
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

    @Override
    public Cart create(UUID userUuid) {
        Cart newCart = new Cart();
        newCart.setUserUuid(userUuid);
        newCart.setTotalPrice(BigDecimal.ZERO);
        return cartRepository.save(newCart);
    }

    @Override
    @Transactional(readOnly = true)
    public Cart get(UUID userUuid) {
        return cartRepository.findByUserUuid(userUuid)
                .orElseThrow(() -> new CartNotFoundException("Корзина пользователя с uuid = " + userUuid + " не найдена"));
    }

    @Override
    @CacheEvict(value = "cart", key = "#userUuid")
    public Cart addToCart(UUID userUuid, UUID productUuid, int quantity) {
        if (quantity <= 0) {
            throw new IllegalCartStateException("Количество товара не может быть меньше или равно нулю");
        }

        Cart cart = get(userUuid);
        Product product = productService.getByUuid(productUuid);
        updateOrAddItem(cart, product, quantity);
        updateCartTotal(cart);
        return cartRepository.save(cart);
    }

    @Override
    @CacheEvict(value = "cart", key = "#userUuid")
    public Cart removeFromCart(UUID userUuid, UUID productUuid) {
        Cart cart = get(userUuid);
        removeCartItem(cart, productUuid);
        updateCartTotal(cart);
        return cartRepository.save(cart);
    }

    @Override
    @CacheEvict(value = "cart", key = "#userUuid")
    public void clear(UUID userUuid) {
        Cart cart = get(userUuid);
        cart.getItems().clear();
        updateCartTotal(cart);
        cartRepository.save(cart);
    }

    @Override
    @Cacheable(value = "cart", key = "#userUuid")
    @Transactional(readOnly = true)
    public Cart getCachedCart(UUID userUuid) {
        return get(userUuid);
    }

    @Override
    @CacheEvict(value = "cart", key = "#userUuid")
    public void updateQuantity(UUID userUuid, UUID productUuid, int quantity) {
        if (quantity <= 0) {
            throw new IllegalCartStateException("Количество товара не может быть меньше или равно нулю");
        }

        Cart cart = get(userUuid);
        updateItemQuantity(cart, productUuid, quantity);
        updateCartTotal(cart);
        cartRepository.save(cart);
    }

    /**
     * Изменить наличие товара в корзине или добавить новый товар
     *
     * @param cart Корзина
     * @param product Товар
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
     * @param cart Корзина
     * @param productUuid Идентификатор товара
     */
    private void removeCartItem(Cart cart, UUID productUuid) {
        cart.getItems().removeIf(item -> item.getProduct().getUuid().equals(productUuid));
    }

    /**
     * Обновить количество товара
     *
     * @param cart Корзина
     * @param productUuid Идентификатор товара
     * @param quantity Количество товара
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