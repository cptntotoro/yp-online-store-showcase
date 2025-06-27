package ru.practicum.mapper.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.order.OrderItemDto;
import ru.practicum.dto.product.ProductOutDto;
import ru.practicum.model.order.OrderItem;
import ru.practicum.model.product.Product;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class OrderItemMapperTest {

    @InjectMocks
    private OrderItemMapperImpl orderItemMapper;

    @Test
    void shouldMapOrderItemToDto() {
        UUID itemId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setUuid(productId);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(99.99));

        OrderItem orderItem = new OrderItem();
        orderItem.setUuid(itemId);
        orderItem.setProductUuid(product.getUuid());
        orderItem.setQuantity(2);
        orderItem.setPriceAtOrder(BigDecimal.valueOf(199.98));

        ProductOutDto productDto = new ProductOutDto();
        productDto.setUuid(productId);
        productDto.setName("Test Product");

        OrderItemDto dto = orderItemMapper.orderItemToOrderItemDto(orderItem);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(itemId);
        assertThat(dto.getQuantity()).isEqualTo(2);
        assertThat(dto.getPriceAtOrder()).isEqualTo(BigDecimal.valueOf(199.98));
    }

    @Test
    void shouldHandleNullInput() {
        OrderItemDto dto = orderItemMapper.orderItemToOrderItemDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void shouldHandleNullProduct() {
        OrderItem orderItem = new OrderItem();
        orderItem.setUuid(UUID.randomUUID());
        orderItem.setProductUuid(UUID.randomUUID());
        orderItem.setQuantity(1);

        OrderItemDto dto = orderItemMapper.orderItemToOrderItemDto(orderItem);

        assertThat(dto).isNotNull();
        assertThat(dto.getProduct()).isNull();
        assertThat(dto.getQuantity()).isEqualTo(1);
    }

    @Test
    void shouldMapWithPartialData() {
        OrderItem orderItem = new OrderItem();
        orderItem.setUuid(null);
        orderItem.setQuantity(3);

        OrderItemDto dto = orderItemMapper.orderItemToOrderItemDto(orderItem);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isNull();
        assertThat(dto.getQuantity()).isEqualTo(3);
        assertThat(dto.getProduct()).isNull();
        assertThat(dto.getPriceAtOrder()).isNull();
    }
}