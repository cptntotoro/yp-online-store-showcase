package ru.practicum.mapper.order;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.practicum.config.MapperTestConfig;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.dto.order.OrderItemDto;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderItem;
import ru.practicum.model.order.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = MapperTestConfig.class)
class OrderMapperTest {

    @Autowired
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private CartMapper cartMapper;

    @Test
    void shouldMapOrderToDto() {
        // Given
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        OrderItem orderItem = new OrderItem();
        orderItem.setUuid(itemId);

        Order order = new Order();
        order.setUuid(orderId);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(BigDecimal.valueOf(100.50));
        order.setItems(List.of(orderItem));

        // Mock
        when(orderItemMapper.orderItemToOrderItemDto(orderItem))
                .thenReturn(new OrderItemDto());
        when(cartMapper.cartToCartDto(null))
                .thenReturn(null);

        // When
        OrderDto dto = orderMapper.orderToOrderDto(order);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(orderId);
        assertThat(dto.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(dto.getTotalAmount()).isEqualTo(BigDecimal.valueOf(100.50));
        assertThat(dto.getItems()).hasSize(1);
    }

    @Test
    void shouldHandleNullInput() {
        assertThat(orderMapper.orderToOrderDto(null)).isNull();
    }
}
