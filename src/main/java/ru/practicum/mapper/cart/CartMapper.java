package ru.practicum.mapper.cart;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.cart.CartDto;
import ru.practicum.model.cart.Cart;

@Mapper(componentModel = "spring")
public interface CartMapper {

    CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    Cart cartToCartDto(CartDto cartDto);
}
