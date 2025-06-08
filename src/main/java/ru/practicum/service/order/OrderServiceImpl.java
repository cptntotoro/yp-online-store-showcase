package ru.practicum.service.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.order.OrderDao;
import ru.practicum.exception.cart.IllegalCartStateException;
import ru.practicum.exception.order.IllegalOrderStateException;
import ru.practicum.exception.order.OrderNotFoundException;
import ru.practicum.mapper.order.OrderItemMapper;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderItem;
import ru.practicum.model.order.OrderStatus;
import ru.practicum.model.order.OrdersWithTotal;
import ru.practicum.repository.order.OrderItemRepository;
import ru.practicum.repository.order.OrderRepository;
import ru.practicum.service.cart.CartService;
import ru.practicum.service.product.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
     * Сервис управления товарами
     */
    private final ProductService productService;

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
                .filter(cart -> !cart.getItems().isEmpty())
                .switchIfEmpty(Mono.error(new IllegalCartStateException("Нельзя создать заказ из пустой корзины")))
                .flatMap(cart -> {
                    BigDecimal total = calculateCartTotal(cart.getItems());
                    if (total.compareTo(BigDecimal.ZERO) <= 0) {
                        return Mono.error(new IllegalOrderStateException("Сумма заказа должна быть больше нуля"));
                    }

                    return orderRepository.save(orderMapper.orderToOrderDao(
                                    new Order(null, userUuid, cart.getUuid(),
                                            OrderStatus.CREATED, total, null, LocalDateTime.now())))
                            .flatMap(savedOrder -> {
                                List<OrderItem> items = cart.getItems().stream()
                                        .map(ci -> OrderItem.builder()
                                                .orderUuid(savedOrder.getUuid())
                                                .productUuid(ci.getProduct().getUuid())
                                                .quantity(ci.getQuantity())
                                                .priceAtOrder(ci.getProduct().getPrice())
                                                .build())
                                        .collect(Collectors.toList());

                                return orderItemRepository.saveAll(items.stream()
                                                .map(orderItemMapper::orderItemToOrderItemDao)
                                                .collect(Collectors.toList()))
                                        .then(cartService.clear(userUuid))
                                        .thenReturn(orderMapper.orderDaoToOrderWithItems(savedOrder, items));
                            });
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

    @Override
    public Mono<OrdersWithTotal> getUserOrdersWithProducts(UUID userUuid) {
        return orderRepository.findByUserUuid(userUuid)
                .flatMap(this::enrichOrderWithItems)
                .collectList()
                .flatMap(orders -> {
                    if (orders.isEmpty()) {
                        return Mono.just(OrdersWithTotal.builder()
                                .orders(List.of())
                                .products(Map.of())
                                .totalAmount(BigDecimal.ZERO)
                                .build());
                    }

                    Set<UUID> allProductIds = orders.stream()
                            .flatMap(order -> order.getItems().stream()
                                    .map(OrderItem::getProductUuid))
                            .collect(Collectors.toSet());

                    return productService.getProductsByIds(allProductIds)
                            .zipWith(this.getUserTotalAmount(userUuid))
                            .map(tuple -> OrdersWithTotal.builder()
                                    .orders(orders)
                                    .products(tuple.getT1())
                                    .totalAmount(tuple.getT2())
                                    .build());
                });
    }

    /**
     * Обновить статус заказа
     *
     * @param userUuid  Идентификатор пользователя
     * @param orderUuid Идентификатор заказа
     * @param newStatus Новый статус
     */
    private Mono<Void> updateStatus(UUID userUuid, UUID orderUuid, OrderStatus newStatus) {
        return orderRepository.findByUuidAndUserUuid(orderUuid, userUuid)
                .switchIfEmpty(Mono.error(new OrderNotFoundException("Заказ не найден и не был обновлен.")))
                .flatMap(orderDao -> {
                    validateStatusTransition(orderDao.getStatus(), newStatus);
                    orderDao.setStatus(newStatus);
                    return orderRepository.save(orderDao).then();
                });
    }

    /**
     * Наполнить заказы товарами
     *
     * @param orderDao DAO заказа
     * @return Заказ
     */
    private Mono<Order> enrichOrderWithItems(OrderDao orderDao) {
        return orderItemRepository.findByOrderUuid(orderDao.getUuid())
                .flatMap(orderItemDao ->
                        productService.getByUuid(orderItemDao.getProductUuid())
                                .map(productDao -> OrderItem.builder()
                                        .uuid(orderItemDao.getUuid())
                                        .productUuid(productDao.getUuid())
                                        .quantity(orderItemDao.getQuantity())
                                        .priceAtOrder(orderItemDao.getPriceAtOrder())
                                        .build())
                )
                .collectList()
                .map(items -> {
                    Order order = orderMapper.orderDaoToOrder(orderDao);
                    order.setItems(items);
                    return order;
                });
    }

    /**
     * Проверить валидность смены статуса заказа
     *
     * @param current   Текущий статус
     * @param newStatus Новый статус
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus newStatus) {
        if (!ALLOWED_STATUS_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(OrderStatus.class)).contains(newStatus)) {
            throw new IllegalOrderStateException(
                    String.format("Недопустимый переход статуса заказа из %s в %s", current, newStatus)
            );
        }
    }

    /**
     * Расчитать стоимость товаров в корзине / заказе
     *
     * @param cartItems Список товаров корзины
     * @return Стоимость товаров в корзине / заказе
     */
    private BigDecimal calculateCartTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
