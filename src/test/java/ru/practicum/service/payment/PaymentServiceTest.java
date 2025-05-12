package ru.practicum.service.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.exception.payment.PaymentProcessingException;
import ru.practicum.service.order.OrderService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private final UUID testUserUuid = UUID.randomUUID();
    private final UUID testOrderUuid = UUID.randomUUID();
    private final String validCardNumber = "1234567890123456";

    @Test
    void checkout_ShouldCallOrderService_WhenCardNumberIsValid() {
        paymentService.checkout(testUserUuid, testOrderUuid, validCardNumber);

        verify(orderService, times(1)).checkout(testUserUuid, testOrderUuid);
    }

    @Test
    void checkout_ShouldThrowException_WhenCardNumberIsNull() {
        assertThrows(PaymentProcessingException.class,
                () -> paymentService.checkout(testUserUuid, testOrderUuid, null));

        verify(orderService, never()).checkout(any(), any());
    }

    @Test
    void checkout_ShouldThrowException_WhenCardNumberIsTooShort() {
        String shortCardNumber = "123456789012345";

        assertThrows(PaymentProcessingException.class,
                () -> paymentService.checkout(testUserUuid, testOrderUuid, shortCardNumber));

        verify(orderService, never()).checkout(any(), any());
    }

    @Test
    void checkout_ShouldThrowException_WhenCardNumberIsTooLong() {
        String longCardNumber = "12345678901234567";

        assertThrows(PaymentProcessingException.class,
                () -> paymentService.checkout(testUserUuid, testOrderUuid, longCardNumber));

        verify(orderService, never()).checkout(any(), any());
    }

    @Test
    void checkout_ShouldThrowException_WhenCardNumberContainsNonDigits() {
        String invalidCardNumber = "1234abcd56789012";

        assertThrows(PaymentProcessingException.class,
                () -> paymentService.checkout(testUserUuid, testOrderUuid, invalidCardNumber));

        verify(orderService, never()).checkout(any(), any());
    }

    @Test
    void validateCardNumber_ShouldNotThrow_WhenCardNumberIsValid() {
        assertDoesNotThrow(() -> paymentService.checkout(testUserUuid, testOrderUuid, validCardNumber));
    }
}
