package ru.practicum.mapper.cart;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.cart.CartItemDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.cart.CartItem;

/**
 * Маппер товаров корзины
 */
@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface CartItemMapper {

    /**
     * Смаппить товар из корзины в DTO товара из корзины
     *
     * @param cartItemDto Товар из корзины
     * @return DTO товара из корзины
     */
    @Mapping(target = "product", source = "product")
    CartItemDto cartItemToCartItemDto(CartItem cartItemDto);
}
