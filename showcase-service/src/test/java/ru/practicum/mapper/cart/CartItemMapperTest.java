package ru.practicum.mapper.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dao.cart.CartItemDao;
import ru.practicum.dto.cart.CartItemDto;
import ru.practicum.dto.cart.cache.CartItemCacheDto;
import ru.practicum.dto.product.ProductOutDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.product.Product;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartItemMapperTest {

    private CartItemMapper cartItemMapper;

    @Mock
    private ProductMapper productMapper;

    @BeforeEach
    void setUp() throws Exception {
        cartItemMapper = Mappers.getMapper(CartItemMapper.class);
        injectDependencies(cartItemMapper);
    }

    private void injectDependencies(Object mapper) throws Exception {
        for (Field field : mapper.getClass().getDeclaredFields()) {
            if (field.getType().equals(ProductMapper.class)) {
                field.setAccessible(true);
                field.set(mapper, productMapper);
            }
        }
    }

    @Test
    void shouldMapCartItemToDto() {
        UUID productId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Product product = new Product();
        product.setUuid(productId);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100.50));

        CartItem cartItem = new CartItem();
        cartItem.setUuid(itemId);
        cartItem.setProduct(product);
        cartItem.setQuantity(3);

        ProductOutDto productDto = new ProductOutDto();
        productDto.setUuid(productId);
        productDto.setName("Test Product");
        productDto.setPrice(BigDecimal.valueOf(100.50));

        when(productMapper.productToProductOutDto(product)).thenReturn(productDto);

        CartItemDto dto = cartItemMapper.cartItemToCartItemDto(cartItem);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(itemId);
        assertThat(dto.getQuantity()).isEqualTo(3);
        assertThat(dto.getProduct()).isNotNull();
        assertThat(dto.getProduct().getUuid()).isEqualTo(productId);
        assertThat(dto.getTotalPrice()).isEqualTo(BigDecimal.valueOf(301.50));
    }

    @Test
    void shouldMapCartItemToDao() {
        UUID productId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Product product = new Product();
        product.setUuid(productId);

        CartItem cartItem = new CartItem();
        cartItem.setUuid(itemId);
        cartItem.setCartUuid(cartId);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setCreatedAt(now);

        CartItemDao dao = cartItemMapper.cartItemToCartItemDao(cartItem);

        assertThat(dao).isNotNull();
        assertThat(dao.getUuid()).isEqualTo(itemId);
        assertThat(dao.getCartUuid()).isEqualTo(cartId);
        assertThat(dao.getProductUuid()).isEqualTo(productId);
        assertThat(dao.getQuantity()).isEqualTo(2);
        assertThat(dao.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldMapDaoToCartItem() {
        UUID productId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        CartItemDao dao = new CartItemDao();
        dao.setUuid(itemId);
        dao.setCartUuid(cartId);
        dao.setProductUuid(productId);
        dao.setQuantity(4);
        dao.setCreatedAt(now);

        CartItem cartItem = cartItemMapper.cartItemDaoToCartItem(dao);

        assertThat(cartItem).isNotNull();
        assertThat(cartItem.getUuid()).isEqualTo(itemId);
        assertThat(cartItem.getCartUuid()).isEqualTo(cartId);
        assertThat(cartItem.getProduct()).isNotNull();
        assertThat(cartItem.getProduct().getUuid()).isEqualTo(productId);
        assertThat(cartItem.getQuantity()).isEqualTo(4);
        assertThat(cartItem.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldMapCartItemToCacheDto() {
        UUID productId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        Product product = new Product();
        product.setUuid(productId);

        CartItem cartItem = new CartItem();
        cartItem.setUuid(itemId);
        cartItem.setCartUuid(cartId);
        cartItem.setProduct(product);
        cartItem.setQuantity(5);

        CartItemCacheDto cacheDto = cartItemMapper.cartItemToCartItemCacheDto(cartItem);

        assertThat(cacheDto).isNotNull();
        assertThat(cacheDto.getUuid()).isEqualTo(itemId);
        assertThat(cacheDto.getCartUuid()).isEqualTo(cartId);
        assertThat(cacheDto.getProductUuid()).isEqualTo(productId);
        assertThat(cacheDto.getQuantity()).isEqualTo(5);
    }

    @Test
    void shouldMapCacheDtoToCartItem() {
        UUID productId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        CartItemCacheDto cacheDto = new CartItemCacheDto();
        cacheDto.setUuid(itemId);
        cacheDto.setCartUuid(cartId);
        cacheDto.setProductUuid(productId);
        cacheDto.setQuantity(6);

        CartItem cartItem = cartItemMapper.cartItemCacheDtoToCartItem(cacheDto);

        assertThat(cartItem).isNotNull();
        assertThat(cartItem.getUuid()).isEqualTo(itemId);
        assertThat(cartItem.getCartUuid()).isEqualTo(cartId);
        assertThat(cartItem.getProduct()).isNotNull();
        assertThat(cartItem.getProduct().getUuid()).isEqualTo(productId);
        assertThat(cartItem.getQuantity()).isEqualTo(6);
        assertThat(cartItem.getCreatedAt()).isNull();
    }

    @Test
    void shouldHandleNullInputs() {
        assertThat(cartItemMapper.cartItemToCartItemDto(null)).isNull();
        assertThat(cartItemMapper.cartItemToCartItemDao(null)).isNull();
        assertThat(cartItemMapper.cartItemDaoToCartItem(null)).isNull();
        assertThat(cartItemMapper.cartItemToCartItemCacheDto(null)).isNull();
        assertThat(cartItemMapper.cartItemCacheDtoToCartItem(null)).isNull();
    }
}