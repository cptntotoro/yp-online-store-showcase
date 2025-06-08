package ru.practicum.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.exception.cart.IllegalCartStateException;
import ru.practicum.exception.order.IllegalOrderStateException;
import ru.practicum.exception.order.OrderNotFoundException;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderItem;
import ru.practicum.model.order.OrderStatus;
import ru.practicum.repository.order.OrderRepository;
import ru.practicum.service.cart.CartService;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    /**
     * Репозиторий заказов
     */
    private final OrderRepository orderRepository;

    /**
     * Сервис управления корзиной товаров
     */
    private final CartService cartService;

    /**
     * Валидные переключения статусов
     */
    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_STATUS_TRANSITIONS = Map.of(
            OrderStatus.CREATED, EnumSet.of(OrderStatus.PAID, OrderStatus.CANCELLED),
            OrderStatus.PAID, EnumSet.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED)
    );

    @Override
    @Transactional
    public Mono<Order> create(UUID userUuid) {
        return cartService.get(userUuid)
                .flatMap(cart -> {
                    if (cart.getItems().isEmpty()) {
                        return Mono.error(new IllegalCartStateException("Нельзя создать заказ из пустой корзины"));
                    }

                    Order order = new Order(userUuid, cart);
                    BigDecimal calculatedTotal = calculateOrderTotal(order.getItems());

                    if (calculatedTotal.compareTo(BigDecimal.ZERO) <= 0) {
                        return Mono.error(new IllegalOrderStateException("Сумма заказа должна быть больше нуля"));
                    }

                    order.setTotalPrice(calculatedTotal);
                    return orderRepository.save(order)
                            .doOnSuccess(o -> cartService.clear(userUuid).subscribe()); // очищаем корзину
                });
    }

    @Override
    public Flux<Order> getUserOrders(UUID userUuid) {
        return orderRepository.findByUserUuid(userUuid);
    }

    @Override
    public Mono<Order> getByUuid(UUID userUuid, UUID uuid) {
        return orderRepository.findByIdWhereUserUuidIn(uuid, userUuid)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Заказ не найден")));
    }

    @Override
    @Transactional
    public Mono<Void> checkout(UUID userUuid, UUID orderUuid) {
        return updateStatus(userUuid, orderUuid, OrderStatus.PAID);
    }

    @Override
    public Mono<Void> cancel(UUID userUuid, UUID orderUuid) {
        return updateStatus(userUuid, orderUuid, OrderStatus.CANCELLED);
    }

    @Override
    public Mono<BigDecimal> getUserTotalAmount(UUID userUuid) {
        return orderRepository.getTotalOrdersAmountByUser(userUuid);
    }

    private Mono<Void> updateStatus(UUID userUuid, UUID orderUuid, OrderStatus newStatus) {
        return orderRepository.findByUuidAndUserUuid(orderUuid, userUuid)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Заказ не найден и не был обновлен.")))
                .flatMap(order -> {
                    validateStatusTransition(order.getStatus(), newStatus);
                    order.setStatus(newStatus);
                    return orderRepository.save(order).then();
                });
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus newStatus) {
        if (!ALLOWED_STATUS_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(OrderStatus.class)).contains(newStatus)) {
            throw new IllegalOrderStateException(
                    String.format("Недопустимый переход статуса заказа из %s в %s", current, newStatus)
            );
        }
    }

    /**
     * Расчитать стоимость товаров в заказе
     *
     * @param items Список товаров заказа
     * @return Стоимость товаров в заказе
     */
    private BigDecimal calculateOrderTotal(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
