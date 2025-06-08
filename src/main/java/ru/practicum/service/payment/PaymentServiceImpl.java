package ru.practicum.service.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
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
    public Mono<Void> checkout(UUID userUuid, UUID orderUuid, String cardNumber) {
        return Mono.defer(() -> {
            if (cardNumber == null || !cardNumber.matches(CARD_FORMAT_REGEX)) {
                return Mono.error(new PaymentProcessingException("Некорректный номер карты."));
            }
            return orderService.checkout(userUuid, orderUuid);
        });
    }
}
