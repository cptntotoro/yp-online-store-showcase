package ru.practicum.service.cart;

import ru.practicum.model.cart.Cart;

import java.math.BigDecimal;
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
    Cart create(UUID userUuid);

    /**
     * Получить корзину
     *
     * @param userUuid Идентификатор пользователя
     * @return Корзина
     */
    Cart get(UUID userUuid);

    /**
     * Добавить товор в корзину
     *
     * @param userUuid Идентификатор пользователя
     * @param productUuid Идентификатор товара
     * @param quantity Количество
     * @return Корзина
     */
    Cart addToCart(UUID userUuid, UUID productUuid, int quantity);

    /**
     * Удалить товор из корзины
     *
     * @param userUuid Идентификатор пользователя
     * @param productUuid Идентификатор товара
     */
    Cart removeFromCart(UUID userUuid, UUID productUuid);

//    /**
//     * Изменить количество товара в корзине
//     *
//     * @param productUuid Идентификатор товара
//     * @param quantity Количество товара
//     */
//    void updateQuantity(UUID productUuid, int quantity);
//
//    /**
//     * Получить все товары в корзине
//     *
//     * @return Товары в корзине
//     */
//    List<CartItem> getAll();

    /**
     * Очистить корзину товаров
     *
     * @param userUuid Идентификатор пользователя
     */
    void clear(UUID userUuid);

    /**
     * Получить кеш стоимости корзины
     *
     * @param userUuid Идентификатор пользователя
     * @return Стоимость корзины
     */
    BigDecimal getCachedCartTotal(UUID userUuid);
}
