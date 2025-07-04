package ru.practicum.service.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.client.PaymentServiceClientImpl;
import ru.practicum.exception.order.IllegalOrderStateException;
import ru.practicum.exception.order.OrderNotFoundException;
import ru.practicum.model.balance.UserBalance;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderPaymentServiceTest {

    @Mock
    private OrderService orderService;

    @Mock
    private PaymentServiceClientImpl paymentWebClient;

    @InjectMocks
    private OrderPaymentServiceImpl orderPaymentService;

    private final UUID testUserId = UUID.randomUUID();
    private final UUID testOrderId = UUID.randomUUID();
    private final String testCardNumber = "1234567890123456";

    @Test
    void processPayment_ShouldProcessPayment_WhenOrderExistsAndCreated() {
        Order testOrder = createTestOrder(OrderStatus.CREATED, BigDecimal.TEN);

        when(orderService.getByUuid(testUserId, testOrderId))
                .thenReturn(Mono.just(testOrder));
        when(paymentWebClient.processPayment(testUserId, testOrderId, BigDecimal.TEN))
                .thenReturn(Mono.empty());
        when(orderService.save(any(Order.class)))
                .thenReturn(Mono.just(testOrder));

        StepVerifier.create(orderPaymentService.processPayment(testUserId, testOrderId, testCardNumber))
                .verifyComplete();
    }

    @Test
    void processPayment_ShouldThrowException_WhenOrderNotFound() {
        when(orderService.getByUuid(testUserId, testOrderId))
                .thenReturn(Mono.empty());

        StepVerifier.create(orderPaymentService.processPayment(testUserId, testOrderId, testCardNumber))
                .expectError(OrderNotFoundException.class)
                .verify();
    }

    @Test
    void processPayment_ShouldThrowException_WhenOrderNotInCreatedState() {
        Order testOrder = createTestOrder(OrderStatus.PAID, BigDecimal.TEN);

        when(orderService.getByUuid(testUserId, testOrderId))
                .thenReturn(Mono.just(testOrder));

        StepVerifier.create(orderPaymentService.processPayment(testUserId, testOrderId, testCardNumber))
                .expectError(IllegalOrderStateException.class)
                .verify();
    }

    @Test
    void cancel_ShouldProcessRefund_WhenOrderIsPaid() {
        Order testOrder = createTestOrder(OrderStatus.PAID, BigDecimal.TEN);

        when(orderService.getByUuid(testUserId, testOrderId))
                .thenReturn(Mono.just(testOrder));
        when(paymentWebClient.processRefund(testUserId, testOrderId, BigDecimal.TEN))
                .thenReturn(Mono.empty());
        when(orderService.save(any(Order.class)))
                .thenReturn(Mono.just(testOrder));

        StepVerifier.create(orderPaymentService.cancel(testUserId, testOrderId))
                .verifyComplete();
    }

    @Test
    void cancel_ShouldOnlyUpdateStatus_WhenOrderNotPaid() {
        Order testOrder = createTestOrder(OrderStatus.CREATED, BigDecimal.TEN);

        when(orderService.getByUuid(testUserId, testOrderId))
                .thenReturn(Mono.just(testOrder));
        when(orderService.save(any(Order.class)))
                .thenReturn(Mono.just(testOrder));

        StepVerifier.create(orderPaymentService.cancel(testUserId, testOrderId))
                .verifyComplete();
    }

    @Test
    void isBalanceSufficient_ShouldReturnTrue_WhenBalanceIsSufficient() {
        Order testOrder = createTestOrder(OrderStatus.CREATED, BigDecimal.TEN);
        UserBalance userBalance = new UserBalance(testUserId, BigDecimal.valueOf(20));

        when(orderService.getByUuid(testUserId, testOrderId))
                .thenReturn(Mono.just(testOrder));
        when(paymentWebClient.getBalance(testUserId))
                .thenReturn(Mono.just(userBalance));

        StepVerifier.create(orderPaymentService.isBalanceSufficient(testUserId, testOrderId))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void isBalanceSufficient_ShouldReturnFalse_WhenBalanceIsInsufficient() {
        Order testOrder = createTestOrder(OrderStatus.CREATED, BigDecimal.TEN);
        UserBalance userBalance = new UserBalance(testUserId, BigDecimal.valueOf(5));

        when(orderService.getByUuid(testUserId, testOrderId))
                .thenReturn(Mono.just(testOrder));
        when(paymentWebClient.getBalance(testUserId))
                .thenReturn(Mono.just(userBalance));

        StepVerifier.create(orderPaymentService.isBalanceSufficient(testUserId, testOrderId))
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    void checkHealth_ShouldReturnTrue_WhenPaymentServiceIsHealthy() {
        when(paymentWebClient.checkHealth())
                .thenReturn(Mono.just(true));

        StepVerifier.create(orderPaymentService.checkHealth())
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void checkHealth_ShouldReturnFalse_WhenPaymentServiceIsUnhealthy() {
        when(paymentWebClient.checkHealth())
                .thenReturn(Mono.just(false));

        StepVerifier.create(orderPaymentService.checkHealth())
                .expectNext(false)
                .verifyComplete();
    }

    private Order createTestOrder(OrderStatus status, BigDecimal totalPrice) {
        return Order.builder()
                .uuid(testOrderId)
                .userUuid(testUserId)
                .status(status)
                .totalPrice(totalPrice)
                .build();
    }
}