package ru.practicum.controller.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseControllerTest;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.dto.order.OrderItemDto;
import ru.practicum.dto.product.ProductOutDto;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderItem;
import ru.practicum.model.order.OrderStatus;
import ru.practicum.model.order.OrdersWithTotal;
import ru.practicum.model.product.Product;
import ru.practicum.service.order.OrderPaymentService;
import ru.practicum.service.order.OrderService;
import ru.practicum.service.product.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderViewControllerTest extends BaseControllerTest {

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderPaymentService orderPaymentService;

    @MockBean
    private ProductService productService;

    @MockBean
    private OrderMapper orderMapper;

    @MockBean
    private ProductMapper productMapper;

    private UUID testOrderId;
    private UUID testProductId;

    @BeforeEach
    void setUp() {
        super.baseSetUp();
        testOrderId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
    }

    @Test
    void showOrderList_ShouldReturnOrdersPage() {
        Order order = createTestOrder();
        Map<UUID, Product> products = createTestProducts();
        OrdersWithTotal ordersWithTotal = new OrdersWithTotal(
                List.of(order),
                products,
                BigDecimal.valueOf(100)
        );

        when(orderService.getUserOrdersWithProducts(TEST_USER_UUID))
                .thenReturn(Mono.just(ordersWithTotal));

        when(orderMapper.orderToOrderDtoWithProducts(any(Order.class), anyMap(), eq(productMapper)))
                .thenReturn(createTestOrderDto());

        getWebTestClientWithMockUser().get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void showOrderDetails_ShouldReturnOrderPage() {
        Order order = createTestOrder();
        Map<UUID, Product> products = createTestProducts();
        OrderDto orderDto = createTestOrderDto();

        when(orderService.getByUuid(TEST_USER_UUID, testOrderId))
                .thenReturn(Mono.just(order));

        when(orderPaymentService.checkHealth())
                .thenReturn(Mono.just(true));

        when(productService.getProductsByUuids(anySet()))
                .thenReturn(Mono.just(products));

        when(orderMapper.orderToOrderDtoWithProducts(any(Order.class), anyMap(), eq(productMapper)))
                .thenReturn(orderDto);

        getWebTestClientWithMockUser().get()
                .uri("/orders/" + testOrderId)
                .exchange()
                .expectStatus().isOk()
                .expectBody();
    }

    @Test
    void cancel_ShouldRedirectToOrderPage() {
        when(orderPaymentService.cancel(TEST_USER_UUID, testOrderId))
                .thenReturn(Mono.empty());

        getWebTestClientWithMockUser().get()
                .uri("/orders/" + testOrderId + "/checkout/cancel")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/" + testOrderId);
    }

    private Order createTestOrder() {
        OrderItem item = OrderItem.builder()
                .uuid(UUID.randomUUID())
                .orderUuid(testOrderId)
                .productUuid(testProductId)
                .quantity(2)
                .priceAtOrder(BigDecimal.valueOf(50))
                .build();

        return Order.builder()
                .uuid(testOrderId)
                .userUuid(TEST_USER_UUID)
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.valueOf(100))
                .items(List.of(item))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private Map<UUID, Product> createTestProducts() {
        Product product = Product.builder()
                .uuid(testProductId)
                .name("Test Product")
                .price(BigDecimal.valueOf(50))
                .imageUrl("imageUrl")
                .build();

        return Map.of(testProductId, product);
    }

    private OrderDto createTestOrderDto() {
        OrderItemDto itemDto = OrderItemDto.builder()
                .uuid(UUID.randomUUID())
                .quantity(2)
                .priceAtOrder(BigDecimal.valueOf(50))
                .product(ProductOutDto.builder()
                        .uuid(testProductId)
                        .name("Test Product")
                        .price(BigDecimal.valueOf(50))
                        .imageUrl("imageUrl")
                        .build())
                .build();

        OrderDto dto = new OrderDto();
        dto.setUuid(testOrderId);
        dto.setStatus(OrderStatus.CREATED);
        dto.setTotalPrice(BigDecimal.valueOf(100));
        dto.setItems(List.of(itemDto));
        dto.setCreatedAt(LocalDateTime.now());

        return dto;
    }
}