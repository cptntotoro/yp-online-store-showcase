package ru.practicum.mapper.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.cart.CartDto;
import ru.practicum.dto.cart.CartItemDto;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartMapperTest {

    private CartMapper cartMapper;

    @Mock
    private CartItemMapper cartItemMapper;

    @BeforeEach
    void setUp() throws Exception {
        cartMapper = Mappers.getMapper(CartMapper.class);

        injectDependencies(cartMapper);
    }

    private void injectDependencies(Object mapper) throws Exception {
        for (Field field : mapper.getClass().getDeclaredFields()) {
            if (field.getType().equals(CartItemMapper.class)) {
                field.setAccessible(true);
                field.set(mapper, cartItemMapper);
            }
        }
    }

    @Test
    void shouldMapCartToDto() {
        UUID cartId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        CartItem cartItem = new CartItem();
        cartItem.setUuid(itemId);

        Cart cart = new Cart();
        cart.setUuid(cartId);
        cart.setUserUuid(userId);
        cart.setItems(List.of(cartItem));

        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setUuid(itemId);

        when(cartItemMapper.cartItemToCartItemDto(cartItem))
                .thenReturn(cartItemDto);

        CartDto dto = cartMapper.cartToCartDto(cart);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(cartId);
        assertThat(dto.getItems())
                .hasSize(1)
                .first()
                .extracting(CartItemDto::getUuid)
                .isEqualTo(itemId);
    }

    @Test
    void shouldHandleNullCart() {
        CartDto dto = cartMapper.cartToCartDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void shouldHandleEmptyCart() {
        Cart cart = new Cart();
        cart.setUuid(UUID.randomUUID());
        cart.setItems(List.of());

        CartDto dto = cartMapper.cartToCartDto(cart);

        assertThat(dto).isNotNull();
        assertThat(dto.getItems()).isEmpty();
    }
}
