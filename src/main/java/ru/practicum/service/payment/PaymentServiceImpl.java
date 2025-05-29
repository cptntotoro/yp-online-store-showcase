package ru.practicum.service.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.exception.payment.PaymentProcessingException;
import ru.practicum.service.order.OrderService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final OrderService orderService;

    @Override
    public void checkout(UUID userUuid, UUID orderUuid, String cardNumber) {
        validateCardNumber(cardNumber);
        orderService.checkout(userUuid, orderUuid);
    }

    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            throw new PaymentProcessingException("Некорректный номер карты.");
        }
    }
}
