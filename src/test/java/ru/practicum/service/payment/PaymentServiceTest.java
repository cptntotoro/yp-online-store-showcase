package ru.practicum.service.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.exception.payment.PaymentProcessingException;
import ru.practicum.service.order.OrderService;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private final UUID testUserUuid = UUID.randomUUID();
    private final UUID testOrderUuid = UUID.randomUUID();
    private final String validCardNumber = "1234567812345678";

    @Test
    void checkout_ShouldFail_WhenCardNumberIsNull() {
        StepVerifier.create(paymentService.checkout(testUserUuid, testOrderUuid, null))
                .expectErrorMatches(ex -> ex instanceof PaymentProcessingException &&
                        ex.getMessage().equals("Некорректный номер карты."))
                .verify();

        verifyNoInteractions(orderService);
    }

    @Test
    void checkout_ShouldFail_WhenCardNumberIsInvalid() {
        StepVerifier.create(paymentService.checkout(testUserUuid, testOrderUuid, "invalid"))
                .expectErrorMatches(ex -> ex instanceof PaymentProcessingException &&
                        ex.getMessage().equals("Некорректный номер карты."))
                .verify();

        verifyNoInteractions(orderService);
    }

    @Test
    void checkout_ShouldSuccess_WhenCardIsValid() {
        when(orderService.checkout(testUserUuid, testOrderUuid))
                .thenReturn(Mono.empty());

        StepVerifier.create(paymentService.checkout(testUserUuid, testOrderUuid, validCardNumber))
                .verifyComplete();

        verify(orderService).checkout(testUserUuid, testOrderUuid);
    }

    @Test
    void checkout_ShouldPropagateOrderServiceError() {
        RuntimeException expectedError = new RuntimeException("Order error");
        when(orderService.checkout(testUserUuid, testOrderUuid))
                .thenReturn(Mono.error(expectedError));

        StepVerifier.create(paymentService.checkout(testUserUuid, testOrderUuid, validCardNumber))
                .expectErrorMatches(ex -> ex.equals(expectedError))
                .verify();
    }
}