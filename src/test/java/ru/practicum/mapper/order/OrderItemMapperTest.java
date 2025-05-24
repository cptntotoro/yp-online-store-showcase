package ru.practicum.mapper.order;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.config.MapperTestConfig;
import ru.practicum.dto.order.OrderItemDto;
import ru.practicum.dto.product.ProductDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.order.OrderItem;
import ru.practicum.model.product.Product;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = MapperTestConfig.class)
class OrderItemMapperTest {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Mock
    private ProductMapper productMapper;

    @Test
    void shouldMapOrderItemToDto() throws Exception {
        // Given
        UUID itemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setUuid(productId);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(99.99));

        OrderItem orderItem = new OrderItem();
        orderItem.setUuid(itemId);
        orderItem.setProduct(product);
        orderItem.setQuantity(2);
        orderItem.setPriceAtOrder(BigDecimal.valueOf(199.98));

        ProductDto productDto = new ProductDto();
        productDto.setUuid(productId);
        productDto.setName("Test Product");

        // Mock ProductMapper behavior
        when(productMapper.productToProductDto(product)).thenReturn(productDto);

        // When
        OrderItemDto dto = orderItemMapper.orderItemToOrderItemDto(orderItem);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(itemId);
        assertThat(dto.getQuantity()).isEqualTo(2);
        assertThat(dto.getPriceAtOrder()).isEqualTo(BigDecimal.valueOf(199.98));
        assertThat(dto.getProduct())
                .isNotNull()
                .extracting(ProductDto::getUuid, ProductDto::getName)
                .containsExactly(productId, "Test Product");
    }

    @Test
    void shouldHandleNullInput() {
        // When
        OrderItemDto dto = orderItemMapper.orderItemToOrderItemDto(null);

        // Then
        assertThat(dto).isNull();
    }

    @Test
    void shouldHandleNullProduct() throws Exception {
        // Given
        OrderItem orderItem = new OrderItem();
        orderItem.setUuid(UUID.randomUUID());
        orderItem.setProduct(null);
        orderItem.setQuantity(1);

        // When
        OrderItemDto dto = orderItemMapper.orderItemToOrderItemDto(orderItem);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getProduct()).isNull();
        assertThat(dto.getQuantity()).isEqualTo(1);
    }

    @Test
    void shouldMapWithPartialData() throws Exception {
        // Given
        OrderItem orderItem = new OrderItem();
        orderItem.setUuid(null);
        orderItem.setQuantity(3);
        // product и priceAtOrder не установлены

        // When
        OrderItemDto dto = orderItemMapper.orderItemToOrderItemDto(orderItem);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isNull();
        assertThat(dto.getQuantity()).isEqualTo(3);
        assertThat(dto.getProduct()).isNull();
        assertThat(dto.getPriceAtOrder()).isNull();
    }
}