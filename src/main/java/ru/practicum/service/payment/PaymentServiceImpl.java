package ru.practicum.service.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.exception.payment.PaymentProcessingException;
import ru.practicum.service.order.OrderService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    /**
     * Сервис управления заказами
     */
    private final OrderService orderService;

    /**
     * Номер карты - 16 цифр
     */
    private static final String CARD_FORMAT_REGEX = "\\d{16}";

    @Override
    public void checkout(UUID userUuid, UUID orderUuid, String cardNumber) {
        validateCardNumber(cardNumber);
        orderService.checkout(userUuid, orderUuid);
    }

    /**
     * Проверить номер карты
     *
     * @param cardNumber Номер карты
     */
    private void validateCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches(CARD_FORMAT_REGEX)) {
            throw new PaymentProcessingException("Некорректный номер карты.");
        }
    }
}
