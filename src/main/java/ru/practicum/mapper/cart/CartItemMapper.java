package ru.practicum.mapper.cart;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.cart.CartItemDto;
import ru.practicum.model.cart.CartItem;

@Mapper(componentModel = "spring")
public interface CartItemMapper {
    CartItemMapper INSTANCE = Mappers.getMapper(CartItemMapper.class);

    CartItem cartItemToCartItemDto(CartItemDto cartItemDto);
}
