package ru.practicum.mapper.cart;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.config.MapperTestConfig;
import ru.practicum.dto.cart.CartDto;
import ru.practicum.dto.cart.CartItemDto;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = MapperTestConfig.class)
class CartMapperTest {

    @Autowired
    private CartMapper cartMapper;

    @Mock
    private CartItemMapper cartItemMapper;

    @Test
    void shouldMapCartToDto() {
        // Given
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

        // Mock
        when(cartItemMapper.cartItemToCartItemDto(cartItem))
                .thenReturn(cartItemDto);

        // When
        CartDto dto = cartMapper.cartToCartDto(cart);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(cartId); // Теперь проверяем cart.uuid -> dto.uuid
        assertThat(dto.getItems())
                .hasSize(1)
                .first()
                .extracting(CartItemDto::getUuid)
                .isEqualTo(itemId);
    }

    @Test
    void shouldHandleNullCart() {
        // When
        CartDto dto = cartMapper.cartToCartDto(null);

        // Then
        assertThat(dto).isNull();
    }

    @Test
    void shouldHandleEmptyCart() {
        // Given
        Cart cart = new Cart();
        cart.setUuid(UUID.randomUUID());
        cart.setItems(List.of());

        // When
        CartDto dto = cartMapper.cartToCartDto(cart);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getItems()).isEmpty();
    }
}
