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
                                        .map(cartItem -> OrderItem.builder()
                                                .orderUuid(savedOrder.getUuid())
                                                .productUuid(cartItem.getProduct().getUuid())
                                                .quantity(cartItem.getQuantity())
                                                .priceAtOrder(cartItem.getProduct().getPrice())
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
    public Mono<Order> save(Order order) {
        return orderRepository.save(orderMapper.orderToOrderDao(order))
                .map(orderMapper::orderDaoToOrder);
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

                    return productService.getProductsByUuids(allProductIds)
                            .zipWith(this.getUserTotalAmount(userUuid))
                            .map(tuple -> OrdersWithTotal.builder()
                                    .orders(orders)
                                    .products(tuple.getT1())
                                    .totalAmount(tuple.getT2())
                                    .build());
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
