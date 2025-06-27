package ru.practicum.service.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.cart.CartDao;
import ru.practicum.exception.cart.IllegalCartStateException;
import ru.practicum.mapper.cart.CartItemMapper;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.product.Product;
import ru.practicum.repository.cart.CartItemRepository;
import ru.practicum.repository.cart.CartRepository;
import ru.practicum.service.product.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Сервис управления корзиной товаров
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    /**
     * Репозиторий корзины товаров
     */
    private final CartRepository cartRepository;

    /**
     * Репозиторий товаров козины
     */
    private final CartItemRepository cartItemRepository;

    /**
     * Сервис управления товарами
     */
    private final ProductService productService;

    /**
     * Кеш сервис корзины
     */
    private final CartCacheService cartCacheService;

    /**
     * Маппер корзины товаров
     */
    private final CartMapper cartMapper;

    /**
     * Маппер товаров корзины
     */
    private final CartItemMapper cartItemMapper;


    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Mono<Cart> createGuest(UUID userUuid) {
        Cart newCart = Cart.builder()
                .userUuid(userUuid)
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .build();

        return cartRepository.save(cartMapper.cartToCartDao(newCart))
                .map(cartMapper::cartDaoToCart)
                .onErrorResume(e -> Mono.error(new IllegalCartStateException("Ошибка создания корзины.")));
    }

    @Override
    public Mono<Cart> get(UUID userUuid) {
        return cartCacheService.getCart(userUuid)
                .flatMap(this::fillProductsInCartItems);
    }

    private Mono<Cart> fillProductsInCartItems(Cart cart) {
        return Flux.fromIterable(cart.getItems())
                .flatMap(item -> productService.getByUuid(item.getProduct().getUuid())
                        .map(product -> {
                            item.setProduct(product);
                            return item;
                        }))
                .then()
                .thenReturn(cart);
    }

    @Override
    @Transactional
    public Mono<Cart> addToCart(UUID userUuid, UUID productUuid, int quantity) {
        if (quantity <= 0) {
            return Mono.error(new IllegalCartStateException("Количество товара должно быть больше нуля"));
        }

        return get(userUuid).zipWith(productService.getByUuid(productUuid))
                .flatMap(tuple -> {
                    Cart cart = tuple.getT1();
                    Product product = tuple.getT2();
                    List<CartItem> updatedItems = updateCartItems(cart, product, quantity);
                    cart.setItems(updatedItems);
                    cart.setUpdatedAt(LocalDateTime.now());

                    return updateCartTotal(cart);
                })
                .flatMap(updatedCart -> {
                    CartDao cartDao = cartMapper.cartToCartDao(updatedCart);
                    return cartRepository.save(cartDao)
                            .then(saveCartItems(updatedCart.getItems()))
                            .thenReturn(updatedCart);
                })
                .flatMap(updatedCart -> cartCacheService.evict(userUuid).thenReturn(updatedCart))
                .onErrorResume(e -> Mono.error(new IllegalCartStateException("Не удалось добавить товар в корзину")));
    }

    private List<CartItem> updateCartItems(Cart cart, Product product, int quantity) {
        List<CartItem> updatedItems = new ArrayList<>(cart.getItems());

        updatedItems.stream()
                .filter(item -> item.getProduct().getUuid().equals(product.getUuid()))
                .findFirst()
                .ifPresentOrElse(
                        item -> item.setQuantity(item.getQuantity() + quantity),
                        () -> updatedItems.add(CartItem.builder()
                                .cartUuid(cart.getUuid())
                                .product(product)
                                .quantity(quantity)
                                .build())
                );

        return updatedItems;
    }

    @Override
    @Transactional
    public Mono<Cart> removeFromCart(UUID userUuid, UUID productUuid) {
        return get(userUuid)
                .flatMap(cart -> {
                    List<CartItem> updatedItems = cart.getItems().stream()
                            .filter(item -> !item.getProduct().getUuid().equals(productUuid))
                            .toList();

                    cart.setItems(updatedItems);

                    return updateCartTotal(cart)
                            .flatMap(updatedCart -> {
                                CartDao cartDao = cartMapper.cartToCartDao(updatedCart);
                                cartDao.setUpdatedAt(LocalDateTime.now());
                                return cartRepository.save(cartDao)
                                        .then(cartItemRepository.deleteByCartUuidAndProductUuid(cart.getUuid(), productUuid))
                                        .thenReturn(updatedCart);
                            });
                })
                .flatMap(updatedCart -> cartCacheService.evict(userUuid).thenReturn(updatedCart))
                .onErrorResume(e -> Mono.error(new IllegalCartStateException("Не удалось удалить товар из корзины")));
    }

    @Override
    @Transactional
    public Mono<Void> clear(UUID userUuid) {
        return get(userUuid)
                .flatMap(cart ->
                        cartItemRepository.deleteByCartUuid(cart.getUuid())
                                .then(cartRepository.save(
                                        CartDao.builder()
                                                .uuid(cart.getUuid())
                                                .userUuid(cart.getUserUuid())
                                                .totalPrice(BigDecimal.ZERO)
                                                .updatedAt(LocalDateTime.now())
                                                .build()
                                ))
                )
                .flatMap(cart -> cartCacheService.evict(userUuid))
                .then()
                .onErrorResume(e -> Mono.error(new IllegalCartStateException("Не удалось очистить корзину")));
    }

    @Override
    @Transactional
    public Mono<Cart> updateQuantity(UUID userUuid, UUID productUuid, int quantity) {
        if (quantity <= 0) {
            return Mono.error(new IllegalCartStateException("Количество товара не может быть меньше или равно нулю"));
        }

        return get(userUuid)
                .flatMap(cart -> {
                    List<CartItem> items = new ArrayList<>(cart.getItems());

                    items.stream()
                            .filter(item -> item.getProduct().getUuid().equals(productUuid))
                            .findFirst()
                            .ifPresent(item -> item.setQuantity(quantity));

                    cart.setItems(items);

                    return updateCartTotal(cart)
                            .flatMap(updatedCart -> {
                                CartDao cartDao = cartMapper.cartToCartDao(updatedCart);
                                cartDao.setUpdatedAt(LocalDateTime.now());
                                return cartRepository.save(cartDao)
                                        .then(saveCartItems(updatedCart.getItems()))
                                        .thenReturn(updatedCart);
                            });
                })
                .flatMap(updatedCart -> cartCacheService.evict(userUuid).thenReturn(updatedCart))
                .onErrorResume(e -> Mono.error(new IllegalCartStateException("Не удалось обновить товар в корзине")));
    }

    private Mono<Void> saveCartItems(List<CartItem> items) {
        return Flux.fromIterable(items)
                .map(cartItemMapper::cartItemToCartItemDao)
                .flatMap(cartItemRepository::save)
                .then();
    }

    /**
     * Установить новое значение стоимости товаров в корзине
     *
     * @param cart Корзина
     */
    private Mono<Cart> updateCartTotal(Cart cart) {
        return calculateTotalPrice(cart.getItems())
                .map(total -> {
                    cart.setTotalPrice(total);
                    return cart;
                });
    }

    /**
     * Рассчитать стоимость товаров в корзине
     *
     * @param items Товары
     * @return Стоимость товаров в корзине
     */
    private Mono<BigDecimal> calculateTotalPrice(List<CartItem> items) {
        return Flux.fromIterable(items)
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}