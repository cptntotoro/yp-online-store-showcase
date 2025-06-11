package ru.practicum.service.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
    public Mono<Cart> createGuest(UUID userUuid) {
        Cart cart = Cart.builder()
                .uuid(UUID.randomUUID())
                .userUuid(userUuid)
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return cartRepository.save(cartMapper.cartToCartDao(cart))
                .thenReturn(cart);
    }

    @Override
    public Mono<Cart> get(UUID userUuid) {
        return cartRepository.findByUserUuid(userUuid)
                .flatMap(cartDao ->
                        cartItemRepository.findByCartUuid(cartDao.getUuid())
                                .flatMap(itemDao -> productService.getByUuid(itemDao.getProductUuid())
                                        .map(product -> {
                                            CartItem item = cartItemMapper.cartItemDaoToCartItem(itemDao);
                                            item.setProduct(product);
                                            return item;
                                        }))
                                .collectList()
                                .map(items -> {
                                    Cart cart = cartMapper.cartDaoToCart(cartDao);
                                    cart.setItems(items);
                                    return cart;
                                })
                );
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

                    List<CartItem> items = new ArrayList<>(cart.getItems());

                    Optional<CartItem> existing = items.stream()
                            .filter(item -> item.getProduct().getUuid().equals(product.getUuid()))
                            .findFirst();

                    if (existing.isPresent()) {
                        existing.get().setQuantity(existing.get().getQuantity() + quantity);
                    } else {
                        items.add(new CartItem(UUID.randomUUID(), cart.getUuid(), product, quantity, LocalDateTime.now()));
                    }

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
                .doOnSuccess(saved -> cartCacheService.evict(userUuid));
    }

    @Override
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
                .doOnSuccess(saved -> cartCacheService.evict(userUuid));
    }

    @Override
    public Mono<Void> clear(UUID userUuid) {
        return get(userUuid)
                .flatMap(cart ->
                        cartItemRepository.deleteByCartUuid(cart.getUuid())
                                .then(cartRepository.save(new CartDao(
                                        cart.getUuid(),
                                        cart.getUserUuid(),
                                        BigDecimal.ZERO,
                                        cart.getCreatedAt(),
                                        LocalDateTime.now()
                                )))
                )
                .then()
                .doOnSuccess(unused -> cartCacheService.evict(userUuid));
    }

    @Override
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
                .doOnSuccess(unused -> cartCacheService.evict(userUuid));
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