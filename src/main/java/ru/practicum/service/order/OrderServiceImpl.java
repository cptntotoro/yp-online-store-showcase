package ru.practicum.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.order.OrderDao;
import ru.practicum.dao.order.OrderItemDao;
import ru.practicum.exception.cart.IllegalCartStateException;
import ru.practicum.exception.order.IllegalOrderStateException;
import ru.practicum.exception.order.OrderNotFoundException;
import ru.practicum.mapper.order.OrderItemMapper;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderItem;
import ru.practicum.model.order.OrderStatus;
import ru.practicum.repository.order.OrderItemRepository;
import ru.practicum.repository.order.OrderRepository;
import ru.practicum.service.cart.CartService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    /**
     * Репозиторий заказов
     */
    private final OrderRepository orderRepository;

    /**
     * Репозиторий товаров заказа
     */
    private final OrderItemRepository orderItemRepository;

    /**
     * Сервис управления корзиной товаров
     */
    private final CartService cartService;

    /**
     * Маппер заказов
     */
    private final OrderMapper orderMapper;

    /**
     * Маппер товаров заказа
     */
    private final OrderItemMapper orderItemMapper;

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
                    List<CartItem> items = cart.getItems();
                    if (items.isEmpty()) {
                        return Mono.error(new IllegalCartStateException("Нельзя создать заказ из пустой корзины"));
                    }

                    UUID orderUuid = UUID.randomUUID();
                    LocalDateTime createdAt = LocalDateTime.now();

                    List<OrderItem> orderItems = items.stream()
                            .map(cartItem -> new OrderItem(
                                    UUID.randomUUID(),
                                    orderUuid,
                                    cartItem.getProduct().getUuid(),
                                    cartItem.getQuantity(),
                                    cartItem.getProduct().getPrice()
                            )).toList();

                    BigDecimal total = calculateOrderTotal(orderItems);
                    if (total.compareTo(BigDecimal.ZERO) <= 0) {
                        return Mono.error(new IllegalOrderStateException("Сумма заказа должна быть больше нуля"));
                    }

                    Order order = new Order(orderUuid, userUuid, cart.getUuid(), OrderStatus.CREATED, total,
                            orderItems, createdAt);
                    OrderDao orderDao = orderMapper.orderToOrderDao(order);

                    List<OrderItemDao> orderItemDaos = orderItems.stream()
                            .map(orderItemMapper::orderItemToOrderItemDao)
                            .toList();

                    return orderRepository.save(orderDao)
                            .thenMany(orderItemRepository.saveAll(orderItemDaos))
                            .then(cartService.clear(userUuid))
                            .thenReturn(order);
                });
    }

    @Override
    public Flux<Order> getUserOrders(UUID userUuid) {
        return orderRepository.findByUserUuid(userUuid)
                .flatMap(this::enrichOrderWithItems);
    }

    @Override
    public Mono<Order> getByUuid(UUID userUuid, UUID uuid) {
        return orderRepository.findByUuidAndUserUuid(uuid, userUuid)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Заказ не найден")))
                .flatMap(this::enrichOrderWithItems);
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
                .flatMap(orderDao -> {
                    validateStatusTransition(orderDao.getStatus(), newStatus);
                    orderDao.setStatus(newStatus);
                    return orderRepository.save(orderDao).then();
                });
    }

    private Mono<Order> enrichOrderWithItems(OrderDao orderDao) {
        Order order = orderMapper.orderDaoToOrder(orderDao);
        return orderItemRepository.findByOrderUuid(orderDao.getUuid())
                .map(orderItemMapper::orderItemDaoToOrderItem)
                .collectList()
                .map(orderItems -> {
                    order.setItems(orderItems);
                    return order;
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
