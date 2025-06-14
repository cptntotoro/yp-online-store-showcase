package ru.practicum.service.cart;

import reactor.core.publisher.Mono;
import ru.practicum.model.cart.Cart;

import java.util.UUID;

/**
 * Кеш сервис корзины
 */
public interface CartCacheService {
    Mono<Cart> getCart(UUID userUuid);

    void evict(UUID userUuid);
}
