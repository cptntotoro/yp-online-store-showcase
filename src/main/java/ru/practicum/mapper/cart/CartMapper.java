package ru.practicum.mapper.cart;

import org.mapstruct.Mapper;
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
}
