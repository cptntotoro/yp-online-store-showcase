package ru.practicum.mapper.order;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dao.order.OrderItemDao;
import ru.practicum.dto.order.OrderItemDto;
import ru.practicum.model.order.OrderItem;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderItemMapperTest {

    private final OrderItemMapper mapper = Mappers.getMapper(OrderItemMapper.class);

    @Test
    void shouldMapOrderItemToDto() {
        OrderItem orderItem = OrderItem.builder()
                .uuid(UUID.randomUUID())
                .orderUuid(UUID.randomUUID())
                .productUuid(UUID.randomUUID())
                .quantity(2)
                .priceAtOrder(BigDecimal.valueOf(100))
                .build();

        OrderItemDto dto = mapper.orderItemToOrderItemDto(orderItem);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(orderItem.getUuid());
        assertThat(dto.getQuantity()).isEqualTo(orderItem.getQuantity());
        assertThat(dto.getPriceAtOrder()).isEqualTo(orderItem.getPriceAtOrder());
        assertThat(dto.getProduct()).isNull();
    }

    @Test
    void shouldMapOrderItemToDao() {
        OrderItem orderItem = OrderItem.builder()
                .uuid(UUID.randomUUID())
                .orderUuid(UUID.randomUUID())
                .productUuid(UUID.randomUUID())
                .quantity(3)
                .priceAtOrder(BigDecimal.valueOf(150))
                .build();

        OrderItemDao dao = mapper.orderItemToOrderItemDao(orderItem);

        assertThat(dao).isNotNull();
        assertThat(dao.getUuid()).isEqualTo(orderItem.getUuid());
        assertThat(dao.getOrderUuid()).isEqualTo(orderItem.getOrderUuid());
        assertThat(dao.getProductUuid()).isEqualTo(orderItem.getProductUuid());
        assertThat(dao.getQuantity()).isEqualTo(orderItem.getQuantity());
        assertThat(dao.getPriceAtOrder()).isEqualTo(orderItem.getPriceAtOrder());
    }

    @Test
    void shouldReturnNullWhenMappingNullInput() {
        assertThat(mapper.orderItemToOrderItemDto(null)).isNull();
        assertThat(mapper.orderItemToOrderItemDao(null)).isNull();
    }

    @Test
    void shouldMapOrderItemToDtoWithAllFields() {
        OrderItem orderItem = OrderItem.builder()
                .uuid(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
                .orderUuid(UUID.fromString("223e4567-e89b-12d3-a456-426614174000"))
                .productUuid(UUID.fromString("323e4567-e89b-12d3-a456-426614174000"))
                .quantity(5)
                .priceAtOrder(BigDecimal.valueOf(200.50))
                .build();

        OrderItemDto dto = mapper.orderItemToOrderItemDto(orderItem);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"));
        assertThat(dto.getQuantity()).isEqualTo(5);
        assertThat(dto.getPriceAtOrder()).isEqualTo(BigDecimal.valueOf(200.50));
    }

    @Test
    void shouldMapOrderItemToDaoWithAllFields() {
        OrderItem orderItem = OrderItem.builder()
                .uuid(UUID.fromString("423e4567-e89b-12d3-a456-426614174000"))
                .orderUuid(UUID.fromString("523e4567-e89b-12d3-a456-426614174000"))
                .productUuid(UUID.fromString("623e4567-e89b-12d3-a456-426614174000"))
                .quantity(1)
                .priceAtOrder(BigDecimal.valueOf(50.25))
                .build();

        OrderItemDao dao = mapper.orderItemToOrderItemDao(orderItem);

        assertThat(dao).isNotNull();
        assertThat(dao.getUuid()).isEqualTo(UUID.fromString("423e4567-e89b-12d3-a456-426614174000"));
        assertThat(dao.getOrderUuid()).isEqualTo(UUID.fromString("523e4567-e89b-12d3-a456-426614174000"));
        assertThat(dao.getProductUuid()).isEqualTo(UUID.fromString("623e4567-e89b-12d3-a456-426614174000"));
        assertThat(dao.getQuantity()).isEqualTo(1);
        assertThat(dao.getPriceAtOrder()).isEqualTo(BigDecimal.valueOf(50.25));
    }
}