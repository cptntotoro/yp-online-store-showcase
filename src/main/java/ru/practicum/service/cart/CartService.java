package ru.practicum.service.cart;

import org.springframework.cache.annotation.Cacheable;
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
     * @param user Пользователь
     * @return Корзина
     */
    Cart create(UUID userUuid);

    /**
     * Получить корзину
     *
     * @param userUuid Идентифиактор пользователя
     * @return Корзина
     */
//    Cart get(User user);
    Cart get(UUID userUuid);

    /**
     * Добавить товор в корзину
     *
     * @param user Пользователь
     * @param productUuid Идентификатор товара
     * @param quantity Количество
     * @return Корзина
     */
    Cart addToCart(UUID userUuid, UUID productUuid, int quantity);

    /**
     * Удалить товор из корзины
     *
     * @param user Пользователь
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
     */
    void clear(UUID userUuid);

    @Cacheable(value = "cartTotals", key = "#userUuid")
    BigDecimal getCachedCartTotal(UUID userUuid);

//    /**
//     * Получить количество товаров в корзине
//     *
//     * @return Количество товаров в корзине
//     */
//    int getTotalItems();
//
//    /**
//     * Получить сумму товаров в корзине
//     *
//     * @return Сумма товаров в корзине
//     */
//    BigDecimal getTotalPrice();
}
