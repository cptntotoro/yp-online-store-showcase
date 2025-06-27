package ru.practicum.mapper.cart;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dao.cart.CartDao;
import ru.practicum.dto.cart.cache.CartCacheDto;
import ru.practicum.dto.cart.CartDto;
import ru.practicum.model.cart.Cart;

/**
 * Маппер корзины товаров
 */
@Mapper(componentModel = "spring", uses = CartItemMapper.class)
public interface CartMapper {

    /**
     * Смаппить корзину в DTO корзины
     *
     * @param cartDto Корзина
     * @return DTO корзины
     */
    CartDto cartToCartDto(Cart cartDto);

    /**
     * Смаппить корзину в DAO корзины
     *
     * @param cart Корзина
     * @return DAO корзины
     */
    CartDao cartToCartDao(Cart cart);

    /**
     * Смаппить DAO корзины в корзину
     *
     * @param cartDao DAO корзины
     * @return Корзина
     */
    @Mapping(target = "items", ignore = true)
    Cart cartDaoToCart(CartDao cartDao);

    /**
     * Смаппить корзину в DTO корзины для кеша
     *
     * @param cart Корзина
     * @return DTO корзины для кеша
     */
    CartCacheDto toCacheDto(Cart cart);

    /**
     * Смаппить DTO корзины для кеша в корзину
     *
     * @param cartCacheDto DTO корзины для кеша
     * @return Корзина
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Cart fromCacheDto(CartCacheDto cartCacheDto);
}
