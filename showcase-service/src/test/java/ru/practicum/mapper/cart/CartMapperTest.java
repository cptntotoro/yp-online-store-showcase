package ru.practicum.mapper.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dao.cart.CartDao;
import ru.practicum.dto.cart.CartDto;
import ru.practicum.dto.cart.CartItemDto;
import ru.practicum.dto.cart.cache.CartCacheDto;
import ru.practicum.dto.cart.cache.CartItemCacheDto;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Test
    void shouldMapCartToDao() {
        UUID cartId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Cart cart = new Cart();
        cart.setUuid(cartId);
        cart.setUserUuid(userId);
        cart.setTotalPrice(BigDecimal.valueOf(150.75));
        cart.setCreatedAt(now);
        cart.setUpdatedAt(now);

        CartDao dao = cartMapper.cartToCartDao(cart);

        assertThat(dao).isNotNull();
        assertThat(dao.getUuid()).isEqualTo(cartId);
        assertThat(dao.getUserUuid()).isEqualTo(userId);
        assertThat(dao.getTotalPrice()).isEqualTo(BigDecimal.valueOf(150.75));
        assertThat(dao.getCreatedAt()).isEqualTo(now);
        assertThat(dao.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void shouldMapDaoToCart() {
        UUID cartId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        CartDao dao = new CartDao();
        dao.setUuid(cartId);
        dao.setUserUuid(userId);
        dao.setTotalPrice(BigDecimal.valueOf(200.00));
        dao.setCreatedAt(now);
        dao.setUpdatedAt(now);

        Cart cart = cartMapper.cartDaoToCart(dao);

        assertThat(cart).isNotNull();
        assertThat(cart.getUuid()).isEqualTo(cartId);
        assertThat(cart.getUserUuid()).isEqualTo(userId);
        assertThat(cart.getTotalPrice()).isEqualTo(BigDecimal.valueOf(200.00));
        assertThat(cart.getCreatedAt()).isEqualTo(now);
        assertThat(cart.getUpdatedAt()).isEqualTo(now);
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void shouldMapCartToCacheDto() {
        UUID cartId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        CartItem cartItem = new CartItem();
        cartItem.setUuid(itemId);

        Cart cart = new Cart();
        cart.setUuid(cartId);
        cart.setUserUuid(userId);
        cart.setItems(List.of(cartItem));
        cart.setTotalPrice(BigDecimal.valueOf(300.25));

        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setUuid(itemId);

        CartCacheDto cacheDto = cartMapper.toCacheDto(cart);

        assertThat(cacheDto).isNotNull();
        assertThat(cacheDto.getUuid()).isEqualTo(cartId);
        assertThat(cacheDto.getUserUuid()).isEqualTo(userId);
        assertThat(cacheDto.getTotalPrice()).isEqualTo(BigDecimal.valueOf(300.25));
        assertThat(cacheDto.getItems()).hasSize(1);
    }

    @Test
    void shouldMapCacheDtoToCart() {
        UUID cartId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        CartItemCacheDto itemCacheDto = new CartItemCacheDto();
        itemCacheDto.setUuid(itemId);

        CartCacheDto cacheDto = new CartCacheDto();
        cacheDto.setUuid(cartId);
        cacheDto.setUserUuid(userId);
        cacheDto.setItems(List.of(itemCacheDto));
        cacheDto.setTotalPrice(BigDecimal.valueOf(400.50));

        Cart cart = cartMapper.fromCacheDto(cacheDto);

        assertThat(cart).isNotNull();
        assertThat(cart.getUuid()).isEqualTo(cartId);
        assertThat(cart.getUserUuid()).isEqualTo(userId);
        assertThat(cart.getTotalPrice()).isEqualTo(BigDecimal.valueOf(400.50));
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getCreatedAt()).isNull();
        assertThat(cart.getUpdatedAt()).isNull();
    }

    @Test
    void shouldHandleNullInputs() {
        assertThat(cartMapper.cartToCartDao(null)).isNull();
        assertThat(cartMapper.cartDaoToCart(null)).isNull();
        assertThat(cartMapper.toCacheDto(null)).isNull();
        assertThat(cartMapper.fromCacheDto(null)).isNull();
    }
}
