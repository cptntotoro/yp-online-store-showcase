package ru.practicum.mapper.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.dto.order.OrderItemDto;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderItem;
import ru.practicum.model.order.OrderStatus;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private CartMapper cartMapper;

    @BeforeEach
    void setUp() throws Exception {
        orderMapper = Mappers.getMapper(OrderMapper.class);

        injectDependencies(orderMapper);
    }

    private void injectDependencies(Object mapper) throws Exception {
        for (Field field : mapper.getClass().getDeclaredFields()) {
            if (field.getType().equals(OrderItemMapper.class)) {
                field.setAccessible(true);
                field.set(mapper, orderItemMapper);
            } else if (field.getType().equals(CartMapper.class)) {
                field.setAccessible(true);
                field.set(mapper, cartMapper);
            }
        }
    }

    @Test
    void shouldMapOrderToDto() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        OrderItem orderItem = new OrderItem();
        orderItem.setUuid(itemId);

        Order order = new Order();
        order.setUuid(orderId);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(BigDecimal.valueOf(100.50));
        order.setItems(List.of(orderItem));

        when(orderItemMapper.orderItemToOrderItemDto(orderItem))
                .thenReturn(new OrderItemDto());

        OrderDto dto = orderMapper.orderToOrderDto(order);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(orderId);
        assertThat(dto.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(dto.getTotalPrice()).isEqualTo(BigDecimal.valueOf(100.50));
        assertThat(dto.getItems()).hasSize(1);
    }
}
