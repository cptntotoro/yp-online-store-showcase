package ru.practicum.service.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.practicum.exception.cart.CartNotFoundException;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.repository.cart.CartRepository;

import java.util.Optional;
import java.util.UUID;

import static ru.practicum.config.CacheConfig.CART_CACHE_NAME;

@Service
@RequiredArgsConstructor
public class CartCacheServiceImpl implements CartCacheService {

    /**
     * Репозиторий корзины товаров
     */
    private final CartRepository cartRepository;

    /**
     * Маппер корзины товаров
     */
    private final CartMapper cartMapper;

    private final CacheManager cacheManager;

    @Override
    public Mono<Cart> getCart(UUID userUuid) {
        return Mono.fromCallable(() -> getCachedCart(userUuid))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::just)
                .onErrorResume(e ->
                        fetchCartFromRepository(userUuid));
    }

    @Override
    public Mono<Void> evict(UUID userUuid) {
        return Mono.fromRunnable(() -> evictFromCache(userUuid))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private Cart getCachedCart(UUID userUuid) {
        return Optional.ofNullable(cacheManager.getCache(CART_CACHE_NAME))
                .map(cache -> cache.get(userUuid.toString(), () -> fetchCartFromRepository(userUuid).block()))
                .orElseGet(() -> fetchCartFromRepository(userUuid).block());
    }

    private Mono<Cart> fetchCartFromRepository(UUID userUuid) {
        return cartRepository.findByUserUuid(userUuid)
                .map(cartMapper::cartDaoToCart)
                .switchIfEmpty(Mono.error(new CartNotFoundException(
                        "Корзина пользователя с UUID = " + userUuid + " не найдена")));
    }

    private void evictFromCache(UUID userUuid) {
        Optional.ofNullable(cacheManager.getCache(CART_CACHE_NAME))
                .ifPresent(cache -> cache.evict(userUuid.toString()));
    }
}
