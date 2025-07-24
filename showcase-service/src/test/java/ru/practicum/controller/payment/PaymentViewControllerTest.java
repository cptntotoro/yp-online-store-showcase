package ru.practicum.controller.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseControllerTest;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.dto.payment.PaymentCheckoutDto;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderStatus;
import ru.practicum.service.order.OrderPaymentService;
import ru.practicum.service.order.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentViewControllerTest extends BaseControllerTest {

    @MockBean
    private OrderService orderService;

    @MockBean
    private OrderPaymentService orderPaymentService;

    @MockBean
    private OrderMapper orderMapper;

    private UUID testOrderId;
    private Order testOrder;
    private OrderDto testOrderDto;

    @BeforeEach
    void setUp() {
        super.baseSetUp();
        testOrderId = UUID.randomUUID();
        testOrder = createTestOrder();
        testOrderDto = createTestOrderDto();

        when(orderPaymentService.checkHealth()).thenReturn(Mono.just(true));
        when(orderMapper.orderToOrderDto(any())).thenReturn(testOrderDto);
    }

    @Test
    void previewOrder_ShouldReturnPaymentView() {
        when(orderService.create(TEST_USER_UUID))
                .thenReturn(Mono.just(testOrder));


        when(orderPaymentService.checkHealth()).thenReturn(Mono.just(true));
        when(orderMapper.orderToOrderDto(any(Order.class))).thenReturn(testOrderDto);
        when(cartService.clear(TEST_USER_UUID)).thenReturn(Mono.empty());

        getWebTestClientWithMockUser().get()
                .uri("/payment/checkout")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void previewExistingOrder_ShouldReturnPaymentView() {
        when(orderService.getByUuid(TEST_USER_UUID, testOrderId))
                .thenReturn(Mono.just(testOrder));

        when(orderPaymentService.checkHealth())
                .thenReturn(Mono.just(true));

        when(orderMapper.orderToOrderDto(any()))
                .thenReturn(testOrderDto);

        getWebTestClientWithMockUser().get()
                .uri("/payment/checkout/created/{orderUuid}", testOrderId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void checkout_ShouldProcessPayment_WhenServiceActiveAndBalanceSufficient() {
        PaymentCheckoutDto checkoutDto = new PaymentCheckoutDto("1234567890123456");

        when(orderService.getByUuid(eq(TEST_USER_UUID), eq(testOrderId)))
                .thenReturn(Mono.just(testOrder));

        when(orderPaymentService.checkHealth())
                .thenReturn(Mono.just(true));

        when(orderPaymentService.isBalanceSufficient(eq(TEST_USER_UUID), eq(testOrderId)))
                .thenReturn(Mono.just(true));

        when(orderPaymentService.processPayment(eq(TEST_USER_UUID), eq(testOrderId), any()))
                .thenReturn(Mono.empty());

        when(orderMapper.orderToOrderDto(any()))
                .thenReturn(testOrderDto);

        getWebTestClientWithMockUser().post()
                .uri("/payment/{orderUuid}/checkout", testOrderId)
                .bodyValue(checkoutDto)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/orders/" + testOrderId);
    }

    @Test
    void checkout_ShouldReturnPaymentView_WhenServiceInactive() {
        PaymentCheckoutDto checkoutDto = new PaymentCheckoutDto("1234567890123456");

        when(orderService.getByUuid(TEST_USER_UUID, testOrderId))
                .thenReturn(Mono.just(testOrder));

        when(orderPaymentService.checkHealth())
                .thenReturn(Mono.just(false));

        when(orderMapper.orderToOrderDto(any()))
                .thenReturn(testOrderDto);

        getWebTestClientWithMockUser().post()
                .uri("/payment/{orderUuid}/checkout", testOrderId)
                .bodyValue(checkoutDto)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void checkout_ShouldReturnPaymentView_WhenBalanceInsufficient() {
        PaymentCheckoutDto checkoutDto = new PaymentCheckoutDto("1234567890123456");

        when(orderService.getByUuid(TEST_USER_UUID, testOrderId))
                .thenReturn(Mono.just(testOrder));

        when(orderPaymentService.checkHealth())
                .thenReturn(Mono.just(true));

        when(orderPaymentService.isBalanceSufficient(TEST_USER_UUID, testOrderId))
                .thenReturn(Mono.just(false));

        when(orderMapper.orderToOrderDto(any()))
                .thenReturn(testOrderDto);

        getWebTestClientWithMockUser().post()
                .uri("/payment/{orderUuid}/checkout", testOrderId)
                .bodyValue(checkoutDto)
                .exchange()
                .expectStatus().isOk();
    }

    private Order createTestOrder() {
        return Order.builder()
                .uuid(testOrderId)
                .userUuid(TEST_USER_UUID)
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private OrderDto createTestOrderDto() {
        OrderDto dto = new OrderDto();
        dto.setUuid(testOrderId);
        dto.setStatus(OrderStatus.CREATED);
        dto.setTotalPrice(BigDecimal.valueOf(100));
        dto.setCreatedAt(LocalDateTime.now());
        return dto;
    }
}