package ru.practicum.mapper.cart;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.config.MapperTestConfig;
import ru.practicum.dto.cart.CartItemDto;
import ru.practicum.dto.product.ProductDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.product.Product;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = MapperTestConfig.class)
class CartItemMapperTest {

    @Autowired
    private CartItemMapper cartItemMapper;

    @Mock
    private ProductMapper productMapper;

    @Test
    void shouldMapCartItemToDto() {
        // Given
        Product product = new Product();
        product.setUuid(UUID.randomUUID());
        product.setName("Test Product");

        CartItem cartItem = new CartItem();
        cartItem.setUuid(UUID.randomUUID());
        cartItem.setProduct(product);
        cartItem.setQuantity(3);

        when(productMapper.productToProductDto(product)).thenReturn(new ProductDto());

        CartItemDto dto = cartItemMapper.cartItemToCartItemDto(cartItem);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(cartItem.getUuid());
    }
}