package ru.practicum.service.cart;

import reactor.core.publisher.Mono;
import ru.practicum.model.cart.Cart;

import java.util.UUID;

/**
 * Кеш сервис корзины
 */
public interface CartCacheService {

    /**
     * Получить корзину пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Корзина
     */
    Mono<Cart> getCart(UUID userUuid);

    /**
     * Удалить корзину пользователя
     *
     * @param userUuid Идентификатор пользователя
     */
    Mono<Void> evict(UUID userUuid);

    /**
     * Добавить корзину в кеш
     *
     * @param cart Корзина
     */
    Mono<Void> cacheCart(Cart cart);
}
