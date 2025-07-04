package ru.practicum.mapper.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dao.order.OrderDao;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.dto.order.OrderItemDto;
import ru.practicum.dto.product.ProductOutDto;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderItem;
import ru.practicum.model.order.OrderStatus;
import ru.practicum.model.product.Product;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderMapperTest {

    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private ProductMapper productMapper;

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
        LocalDateTime now = LocalDateTime.now();

        OrderItem orderItem = new OrderItem();
        orderItem.setUuid(itemId);

        Order order = Order.builder()
                .uuid(orderId)
                .userUuid(UUID.randomUUID())
                .cartUuid(UUID.randomUUID())
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.valueOf(100.50))
                .createdAt(now)
                .items(List.of(orderItem))
                .build();

        when(orderItemMapper.orderItemToOrderItemDto(orderItem))
                .thenReturn(new OrderItemDto());

        OrderDto dto = orderMapper.orderToOrderDto(order);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(orderId);
        assertThat(dto.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(dto.getTotalPrice()).isEqualTo(BigDecimal.valueOf(100.50));
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getItems()).hasSize(1);
    }

    @Test
    void shouldMapOrderToDao() {
        UUID orderId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Order order = Order.builder()
                .uuid(orderId)
                .userUuid(UUID.randomUUID())
                .cartUuid(UUID.randomUUID())
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.valueOf(200.75))
                .createdAt(now)
                .build();

        OrderDao dao = orderMapper.orderToOrderDao(order);

        assertThat(dao).isNotNull();
        assertThat(dao.getUuid()).isEqualTo(orderId);
        assertThat(dao.getUserUuid()).isEqualTo(order.getUserUuid());
        assertThat(dao.getCartUuid()).isEqualTo(order.getCartUuid());
        assertThat(dao.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(dao.getTotalPrice()).isEqualTo(BigDecimal.valueOf(200.75));
        assertThat(dao.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void shouldMapDaoToOrder() {
        UUID orderId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OrderDao dao = OrderDao.builder()
                .uuid(orderId)
                .userUuid(UUID.randomUUID())
                .cartUuid(UUID.randomUUID())
                .status(OrderStatus.DELIVERED)
                .totalPrice(BigDecimal.valueOf(300.25))
                .createdAt(now)
                .build();

        Order order = orderMapper.orderDaoToOrder(dao);

        assertThat(order).isNotNull();
        assertThat(order.getUuid()).isEqualTo(orderId);
        assertThat(order.getUserUuid()).isEqualTo(dao.getUserUuid());
        assertThat(order.getCartUuid()).isEqualTo(dao.getCartUuid());
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getTotalPrice()).isEqualTo(BigDecimal.valueOf(300.25));
        assertThat(order.getCreatedAt()).isEqualTo(now);
        assertThat(order.getItems()).isNull();
    }

    @Test
    void shouldMapDaoToOrderWithItems() {
        UUID orderId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        OrderDao dao = OrderDao.builder()
                .uuid(orderId)
                .userUuid(UUID.randomUUID())
                .cartUuid(UUID.randomUUID())
                .status(OrderStatus.DELIVERED)
                .totalPrice(BigDecimal.valueOf(150.99))
                .createdAt(now)
                .build();

        OrderItem item = new OrderItem();
        item.setUuid(itemId);
        item.setOrderUuid(orderId);

        Order order = orderMapper.orderDaoToOrderWithItems(dao, List.of(item));

        assertThat(order).isNotNull();
        assertThat(order.getUuid()).isEqualTo(orderId);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
        assertThat(order.getTotalPrice()).isEqualTo(BigDecimal.valueOf(150.99));
        assertThat(order.getCreatedAt()).isEqualTo(now);
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().getFirst().getUuid()).isEqualTo(itemId);
    }

    @Test
    void shouldMapDaoToOrderWithItemsWhenItemsNull() {
        OrderDao dao = OrderDao.builder()
                .uuid(UUID.randomUUID())
                .build();

        Order order = orderMapper.orderDaoToOrderWithItems(dao, null);

        assertThat(order).isNotNull();
        assertThat(order.getItems()).isNull();
    }

    @Test
    void shouldMapOrderToDtoWithProducts() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Product product = new Product();
        product.setUuid(productId);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));

        OrderItem orderItem = OrderItem.builder()
                .uuid(itemId)
                .productUuid(productId)
                .quantity(2)
                .priceAtOrder(BigDecimal.valueOf(100))
                .build();

        Order order = Order.builder()
                .uuid(orderId)
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.valueOf(200))
                .createdAt(now)
                .items(List.of(orderItem))
                .build();

        Map<UUID, Product> products = Map.of(productId, product);

        when(productMapper.productToProductOutDto(product))
                .thenReturn(
                        OrderItemDto.builder()
                                .product(new ProductOutDto(
                                                productId,
                                                "Test Product",
                                                "Description",
                                                BigDecimal.valueOf(100),
                                                "image.jpg"
                                        )
                                )
                                .build()
                                .getProduct());

        OrderDto dto = orderMapper.orderToOrderDtoWithProducts(order, products, productMapper);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(orderId);
        assertThat(dto.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(dto.getTotalPrice()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getItems()).hasSize(1);

        OrderItemDto itemDto = dto.getItems().getFirst();
        assertThat(itemDto.getUuid()).isEqualTo(itemId);
        assertThat(itemDto.getQuantity()).isEqualTo(2);
        assertThat(itemDto.getPriceAtOrder()).isEqualTo(BigDecimal.valueOf(100));
        assertThat(itemDto.getProduct()).isNotNull();
        assertThat(itemDto.getProduct().getUuid()).isEqualTo(productId);
        assertThat(itemDto.getProduct().getName()).isEqualTo("Test Product");

        verify(productMapper).productToProductOutDto(product);
    }

    @Test
    void shouldMapOrderToDtoWithProductsWhenProductNotFound() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();

        OrderItem orderItem = OrderItem.builder()
                .uuid(itemId)
                .productUuid(productId)
                .quantity(1)
                .priceAtOrder(BigDecimal.valueOf(50))
                .build();

        Order order = Order.builder()
                .uuid(orderId)
                .items(List.of(orderItem))
                .build();

        Map<UUID, Product> products = Map.of();

        OrderDto dto = orderMapper.orderToOrderDtoWithProducts(order, products, productMapper);

        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().getFirst().getProduct()).isNull();
        verifyNoInteractions(productMapper);
    }
}