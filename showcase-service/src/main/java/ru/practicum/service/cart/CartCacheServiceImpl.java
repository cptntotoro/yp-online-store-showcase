package ru.practicum.service.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.dto.cart.cache.CartCacheDto;
import ru.practicum.exception.cart.CartNotFoundException;
import ru.practicum.mapper.cart.CartItemMapper;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.repository.cart.CartRepository;
import ru.practicum.repository.cart.CartItemRepository;

import java.time.Duration;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartCacheServiceImpl implements CartCacheService {

    /**
     * Репозиторий корзины товаров
     */
    private final CartRepository cartRepository;

    /**
     * Репозиторий товаров козины
     */
    private final CartItemRepository cartItemRepository;

    /**
     * Маппер корзины товаров
     */
    private final CartMapper cartMapper;

    /**
     * Маппер товаров корзины
     */
    private final CartItemMapper cartItemMapper;

    /**
     * Кеш корзины
     */
    private final ReactiveRedisTemplate<String, CartCacheDto> cartCacheTemplate;

    private static final String CART_KEY_PREFIX = "cart:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    @Override
    public Mono<Cart> getCart(UUID userUuid) {
        return cartCacheTemplate.opsForValue().get(CART_KEY_PREFIX + userUuid)
                .flatMap(dto -> Mono.justOrEmpty(cartMapper.fromCacheDto(dto)))
                .switchIfEmpty(fetchAndCacheCart(userUuid));
    }

    @Override
    public Mono<Void> evict(UUID userUuid) {
        return cartCacheTemplate.delete(CART_KEY_PREFIX + userUuid).then();
    }

    @Override
    public Mono<Void> cacheCart(Cart cart) {
        if (cart == null) {
            return Mono.empty();
        }

        CartCacheDto dto = cartMapper.toCacheDto(cart);
        return cartCacheTemplate.opsForValue()
                .set(CART_KEY_PREFIX + cart.getUserUuid(), dto, CACHE_TTL)
                .then();
    }

    private Mono<Cart> fetchAndCacheCart(UUID userUuid) {
        return cartRepository.findByUserUuid(userUuid)
                .switchIfEmpty(Mono.error(new CartNotFoundException(
                        "Корзина пользователя с UUID = " + userUuid + " не найдена")))
                .flatMap(cartDao -> {
                    Cart cart = cartMapper.cartDaoToCart(cartDao);

                    return cartItemRepository.findByCartUuid(cartDao.getUuid())
                            .collectList()
                            .map(cartItemDaos -> {
                                cart.setItems(cartItemDaos.stream()
                                        .map(cartItemMapper::cartItemDaoToCartItem)
                                        .collect(Collectors.toList()));
                                return cart;
                            });
                })
                .flatMap(cart -> {
                    CartCacheDto dto = cartMapper.toCacheDto(cart);
                    return cartCacheTemplate.opsForValue()
                            .set(CART_KEY_PREFIX + userUuid, dto, CACHE_TTL)
                            .thenReturn(cart);
                });
    }
}