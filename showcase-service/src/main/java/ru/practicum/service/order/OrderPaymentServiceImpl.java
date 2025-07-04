package ru.practicum.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.practicum.client.PaymentServiceClient;
import ru.practicum.exception.order.IllegalOrderStateException;
import ru.practicum.exception.order.OrderNotFoundException;
import ru.practicum.model.order.OrderStatus;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderPaymentServiceImpl implements OrderPaymentService {

    /**
     * Репозиторий заказов
     */
    private final OrderService orderService;

    /**
     * Клиент сервиса оплаты
     */
    private final PaymentServiceClient paymentWebClient;

    @Transactional
    @Override
    public Mono<Void> processPayment(UUID userUuid, UUID orderUuid, String cardNumber) {
        return orderService.getByUuid(userUuid, orderUuid)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Заказ не найден")))
                .flatMap(order -> {
                    if (order.getStatus() != OrderStatus.CREATED) {
                        return Mono.error(new IllegalOrderStateException("Заказ уже обработан или отменен"));
                    }

                    return paymentWebClient.processPayment(userUuid, orderUuid, order.getTotalPrice())
                            .then(updateOrderStatus(userUuid, orderUuid, OrderStatus.PAID));
                });
    }

    @Transactional
    @Override
    public Mono<Void> cancel(UUID userUuid, UUID orderUuid) {
        return orderService.getByUuid(userUuid, orderUuid)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Заказ не найден")))
                .flatMap(order -> {
                    if (order.getStatus() == OrderStatus.PAID) {
                        return paymentWebClient.processRefund(userUuid, orderUuid, order.getTotalPrice())
                                .then(updateOrderStatus(userUuid, orderUuid, OrderStatus.CANCELLED));
                    }
                    return updateOrderStatus(userUuid, orderUuid, OrderStatus.CANCELLED);
                });
    }

    @Override
    public Mono<Boolean> isBalanceSufficient(UUID userUuid, UUID orderUuid) {
        return orderService.getByUuid(userUuid, orderUuid)
                .flatMap(order -> paymentWebClient.getBalance(userUuid)
                        .map(balance -> balance.getBalance().compareTo(order.getTotalPrice()) >= 0));
    }

    @Override
    public Mono<Boolean> checkHealth() {
        return paymentWebClient.checkHealth();
    }

    /**
     * Обновить статус заказа
     *
     * @param userUuid  Идентификатор пользователя
     * @param orderUuid Идентификатор заказа
     * @param newStatus Новый статус
     */
    private Mono<Void> updateOrderStatus(UUID userUuid, UUID orderUuid, OrderStatus newStatus) {
        return orderService.getByUuid(userUuid, orderUuid)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Заказ не найден и не был обновлен.")))
                .flatMap(order -> {
                    order.getStatus().validateTransition(newStatus);
                    order.setStatus(newStatus);
                    return orderService.save(order).then();
                });
    }
}
