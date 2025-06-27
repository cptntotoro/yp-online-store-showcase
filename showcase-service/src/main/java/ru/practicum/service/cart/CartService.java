package ru.practicum.service.cart;

import reactor.core.publisher.Mono;
import ru.practicum.model.cart.Cart;

import java.util.UUID;

/**
 * Сервис управления корзиной товаров
 */
public interface CartService {
    /**
     * Создать корзину
     *
     * @param userUuid Идентификатор пользователя
     * @return Корзина
     */
    Mono<Cart> createGuest(UUID userUuid);

    /**
     * Добавить товор в корзину
     *
     * @param userUuid    Идентификатор пользователя
     * @param productUuid Идентификатор товара
     * @param quantity    Количество
     * @return Корзина
     */
    Mono<Cart> addToCart(UUID userUuid, UUID productUuid, int quantity);

    /**
     * Удалить товор из корзины
     *
     * @param userUuid    Идентификатор пользователя
     * @param productUuid Идентификатор товара
     */
    Mono<Cart> removeFromCart(UUID userUuid, UUID productUuid);

    /**
     * Очистить корзину товаров
     *
     * @param userUuid Идентификатор пользователя
     */
    Mono<Void> clear(UUID userUuid);

    /**
     * Получить корзину пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Корзина
     */
    Mono<Cart> get(UUID userUuid);

    /**
     * Обновить количество товара в корзине
     *
     * @param userUuid    Идентификатор пользователя
     * @param productUuid Идентификатор товара
     * @param quantity    Количество товара
     */
    Mono<Cart> updateQuantity(UUID userUuid, UUID productUuid, int quantity);
}
