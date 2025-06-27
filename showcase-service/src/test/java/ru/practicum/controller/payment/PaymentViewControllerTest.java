package ru.practicum.controller.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseControllerTest;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.exception.cart.IllegalCartStateException;
import ru.practicum.exception.payment.PaymentProcessingException;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.model.order.Order;
import ru.practicum.service.order.OrderService;
import ru.practicum.service.payment.PaymentService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentViewControllerTest extends BaseControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private PaymentViewController paymentViewController;

    private final UUID TEST_ORDER_UUID = UUID.randomUUID();
    private final String TEST_CARD_NUMBER = "4111111111111111";

    @Override
    protected Object getController() {
        return paymentViewController;
    }

    @BeforeEach
    void setUp() {
        super.baseSetUp();
    }

    @Test
    void previewOrder_ShouldReturnPaymentView_WhenOrderCreated() {
        Order order = new Order();
        order.setUuid(TEST_ORDER_UUID);
        OrderDto orderDto = new OrderDto();
        orderDto.setUuid(TEST_ORDER_UUID);

        when(orderService.create(TEST_USER_UUID)).thenReturn(Mono.just(order));
        when(cartService.clear(TEST_USER_UUID)).thenReturn(Mono.empty());
        when(orderMapper.orderToOrderDto(order)).thenReturn(orderDto);

        webTestClient.get()
                .uri("/payment/checkout")
                .exchange()
                .expectStatus().isOk();

        verify(orderService).create(TEST_USER_UUID);
        verify(cartService).clear(TEST_USER_UUID);
        verify(orderMapper).orderToOrderDto(order);
    }

    @Test
    void previewOrder_ShouldReturnError_WhenOrderCreationFails() {
        when(orderService.create(TEST_USER_UUID)).thenReturn(Mono.error(new IllegalCartStateException("Нельзя создать заказ из пустой корзины")));

        webTestClient.get()
                .uri("/payment/checkout")
                .exchange()
                .expectStatus().is4xxClientError();

        verify(orderService).create(TEST_USER_UUID);
    }

    @Test
    void checkout_ShouldRedirectToOrder_WhenPaymentSuccessful() {
        when(paymentService.checkout(TEST_USER_UUID, TEST_ORDER_UUID, TEST_CARD_NUMBER))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/payment/checkout/{orderUuid}")
                        .queryParam("cardNumber", TEST_CARD_NUMBER)
                        .build(TEST_ORDER_UUID))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/" + TEST_ORDER_UUID);

        verify(paymentService).checkout(TEST_USER_UUID, TEST_ORDER_UUID, TEST_CARD_NUMBER);
    }

    @Test
    void checkout_ShouldReturnError_WhenPaymentFails() {
        when(paymentService.checkout(TEST_USER_UUID, TEST_ORDER_UUID, TEST_CARD_NUMBER))
                .thenReturn(Mono.error(new PaymentProcessingException("Некорректный номер карты.")));

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/payment/checkout/{orderUuid}")
                        .queryParam("cardNumber", TEST_CARD_NUMBER)
                        .build(TEST_ORDER_UUID))
                .exchange()
                .expectStatus().is4xxClientError();

        verify(paymentService).checkout(TEST_USER_UUID, TEST_ORDER_UUID, TEST_CARD_NUMBER);
    }

    @Test
    void previewExistingOrder_ShouldReturnPaymentPageWithOrderDetails() {
        Order order = new Order();
        order.setUuid(TEST_ORDER_UUID);
        OrderDto orderDto = new OrderDto();
        orderDto.setUuid(TEST_ORDER_UUID);

        when(orderService.getByUuid(TEST_USER_UUID, TEST_ORDER_UUID))
                .thenReturn(Mono.just(order));
        when(orderMapper.orderToOrderDto(order))
                .thenReturn(orderDto);

        webTestClient.get()
                .uri("/payment/checkout/created/{orderUuid}", TEST_ORDER_UUID)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(result -> {
                    assertNotNull(result.getResponseBody());
                });

        verify(orderService).getByUuid(TEST_USER_UUID, TEST_ORDER_UUID);
        verify(orderMapper).orderToOrderDto(order);
    }

    @Test
    void previewExistingOrder_ShouldReturnError_WhenOrderNotFound() {
        when(orderService.getByUuid(TEST_USER_UUID, TEST_ORDER_UUID))
                .thenReturn(Mono.error(new RuntimeException("Order not found")));

        webTestClient.get()
                .uri("/payment/checkout/created/{orderUuid}", TEST_ORDER_UUID)
                .exchange()
                .expectStatus().is5xxServerError();

        verify(orderService).getByUuid(TEST_USER_UUID, TEST_ORDER_UUID);
        verifyNoInteractions(orderMapper);
    }
}