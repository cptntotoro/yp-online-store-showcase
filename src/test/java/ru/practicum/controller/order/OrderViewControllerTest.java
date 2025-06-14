package ru.practicum.controller.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseControllerTest;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderStatus;
import ru.practicum.service.order.OrderService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderViewControllerTest extends BaseControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderViewController orderViewController;

    private final UUID testOrderUuid = UUID.randomUUID();
    private final BigDecimal testTotalAmount = new BigDecimal("99.99");

    @Override
    protected Object getController() {
        return orderViewController;
    }

    @BeforeEach
    void setUp() {
        super.baseSetUp();
    }

    @Test
    void showOrderList_ShouldReturnOrdersView_WhenDataExists() {
        Order order = new Order();
        OrderDto orderDto = OrderDto.builder()
                .uuid(testOrderUuid)
                .status(OrderStatus.CREATED)
                .build();
        List<Order> orders = List.of(order);

        when(orderService.getUserOrders(TEST_USER_UUID)).thenReturn(Flux.fromIterable(orders));
        when(orderService.getUserTotalAmount(TEST_USER_UUID)).thenReturn(Mono.just(testTotalAmount));
        when(orderMapper.orderToOrderDto(order)).thenReturn(orderDto);

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();

        verify(orderService).getUserOrders(TEST_USER_UUID);
        verify(orderService).getUserTotalAmount(TEST_USER_UUID);
        verify(orderMapper).orderToOrderDto(order);
    }

    @Test
    void showOrderList_ShouldReturnEmptyOrdersView_WhenNoData() {
        when(orderService.getUserOrders(TEST_USER_UUID)).thenReturn(Flux.empty());
        when(orderService.getUserTotalAmount(TEST_USER_UUID)).thenReturn(Mono.just(BigDecimal.ZERO));

        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk();

        verify(orderService).getUserOrders(TEST_USER_UUID);
        verify(orderService).getUserTotalAmount(TEST_USER_UUID);
    }

    @Test
    void showOrderDetails_ShouldReturnOrderView_WhenOrderExists() {
        Order order = new Order();
        OrderDto orderDto = OrderDto.builder()
                .uuid(testOrderUuid)
                .status(OrderStatus.CREATED)
                .build();

        when(orderService.getByUuid(TEST_USER_UUID, testOrderUuid)).thenReturn(Mono.just(order));
        when(orderMapper.orderToOrderDto(order)).thenReturn(orderDto);

        webTestClient.get()
                .uri("/orders/" + testOrderUuid)
                .exchange()
                .expectStatus().isOk();

        verify(orderService).getByUuid(TEST_USER_UUID, testOrderUuid);
        verify(orderMapper).orderToOrderDto(order);
    }

    @Test
    void cancel_ShouldRedirectToOrderPage_WhenCancellationSuccess() {
        when(orderService.cancel(TEST_USER_UUID, testOrderUuid)).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/orders/checkout/cancel/" + testOrderUuid)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/" + testOrderUuid);

        verify(orderService).cancel(TEST_USER_UUID, testOrderUuid);
    }
}