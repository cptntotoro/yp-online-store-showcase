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
    @Transactional(propagation = Propagation.MANDATORY)
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
    @Transactional
    public Mono<Cart> addToCart(UUID userUuid, UUID productUuid, int quantity) {
        if (quantity <= 0) {
            return Mono.error(new IllegalCartStateException("Количество товара не может быть меньше или равно нулю"));
        }

        // Получаем корзину или создаем новую, если не существует
        return get(userUuid)
                .flatMap(cart -> productService.getByUuid(productUuid)
                        .flatMap(product -> {
                            // Создаем копию списка элементов для безопасного изменения
                            List<CartItem> updatedItems = new ArrayList<>(cart.getItems());

                            // Ищем существующий товар в корзине
                            Optional<CartItem> existingItem = updatedItems.stream()
                                    .filter(item -> item.getProduct().getUuid().equals(product.getUuid()))
                                    .findFirst();

                            if (existingItem.isPresent()) {
                                // Обновляем количество существующего товара
                                CartItem item = existingItem.get();
                                item.setQuantity(item.getQuantity() + quantity);
                            } else {
                                // Добавляем новый товар в корзину
                                updatedItems.add(CartItem.builder()
                                        .cartUuid(cart.getUuid())
                                        .product(product)
                                        .quantity(quantity)
                                        .build());
                            }

                            // Обновляем корзину
                            cart.setItems(updatedItems);
                            cart.setUpdatedAt(LocalDateTime.now());

                            // Пересчитываем общую сумму и сохраняем изменения
                            return updateCartTotal(cart)
                                    .flatMap(updatedCart -> {
                                        CartDao cartDao = cartMapper.cartToCartDao(updatedCart);
                                        return cartRepository.save(cartDao)
                                                .then(saveCartItems(updatedCart.getItems()))
                                                .thenReturn(updatedCart);
                                    });
                        })
                )
                .doOnSuccess(cart -> {
                    // Инвалидируем кэш после успешного обновления
                    cartCacheService.evict(userUuid);
                });
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
                                .then(cartRepository.save(
                                        CartDao.builder()
                                                .uuid(cart.getUuid())
                                                .userUuid(cart.getUserUuid())
                                                .totalPrice(BigDecimal.ZERO)
                                                .updatedAt(LocalDateTime.now())
                                                .build()
                                ))
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