package ru.practicum.mapper.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.cart.CartItemDto;
import ru.practicum.dto.product.ProductOutDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.product.Product;

import java.lang.reflect.Field;
import java.math.BigDecimal;
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
        // Given
        UUID productId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        Product product = new Product();
        product.setUuid(productId);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100.50)); // Устанавливаем цену продукта

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
    }
}