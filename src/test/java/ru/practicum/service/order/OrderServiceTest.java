package ru.practicum.service.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.order.OrderDao;
import ru.practicum.dao.order.OrderItemDao;
import ru.practicum.exception.order.IllegalOrderStateException;
import ru.practicum.exception.order.OrderNotFoundException;
import ru.practicum.mapper.order.OrderItemMapper;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderItem;
import ru.practicum.model.order.OrderStatus;
import ru.practicum.model.product.Product;
import ru.practicum.repository.order.OrderItemRepository;
import ru.practicum.repository.order.OrderRepository;
import ru.practicum.service.cart.CartService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private CartService cartService;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID userUuid;
    private UUID cartUuid;
    private UUID productUuid;

    private Product product;
    private CartItem cartItem;
    private Cart cart;

    @BeforeEach
    void setup() {
        userUuid = UUID.randomUUID();
        cartUuid = UUID.randomUUID();
        productUuid = UUID.randomUUID();

        product = Product.builder()
                .uuid(productUuid)
                .name("Test")
                .description("desc")
                .price(new BigDecimal("10.00"))
                .build();

        cartItem = CartItem.builder()
                .product(product)
                .quantity(2)
                .build();

        cart = Cart.builder()
                .uuid(cartUuid)
                .userUuid(userUuid)
                .items(List.of(cartItem))
                .build();
    }

    @Test
    void create_ShouldCreateOrder_WhenCartIsValid() {
        UUID orderUuid = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        OrderItem domainItem = new OrderItem(
                UUID.randomUUID(),
                orderUuid,
                productUuid,
                2,
                product.getPrice()
        );
        OrderItemDao daoItem = OrderItemDao.builder()
                .uuid(domainItem.getUuid())
                .orderUuid(orderUuid)
                .productUuid(productUuid)
                .quantity(2)
                .priceAtOrder(product.getPrice())
                .build();

        BigDecimal expectedTotal = domainItem.getTotalPrice();

        Order order = Order.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(cartUuid)
                .status(OrderStatus.CREATED)
                .totalPrice(expectedTotal)
                .items(List.of(domainItem))
                .createdAt(createdAt)
                .build();

        OrderDao orderDao = OrderDao.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(cartUuid)
                .status(OrderStatus.CREATED)
                .totalPrice(expectedTotal)
                .createdAt(createdAt)
                .build();

        // Mocks
        when(cartService.get(userUuid)).thenReturn(Mono.just(cart));
        when(orderMapper.orderToOrderDao(any(Order.class))).thenReturn(orderDao);
        when(orderItemMapper.orderItemToOrderItemDao(any(OrderItem.class))).thenReturn(daoItem);
        when(orderRepository.save(orderDao)).thenReturn(Mono.just(orderDao));
        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.fromIterable(List.of(daoItem)));
        when(cartService.clear(userUuid)).thenReturn(Mono.empty());

        // When
        Mono<Order> resultMono = orderService.create(userUuid);

        // Then
        StepVerifier.create(resultMono)
                .expectNextMatches(result -> {
                    assertEquals(userUuid, result.getUserUuid());
                    assertEquals(cartUuid, result.getCartUuid());
                    assertEquals(OrderStatus.CREATED, result.getStatus());
                    assertEquals(1, result.getItems().size());
                    assertEquals(expectedTotal, result.getTotalPrice());
                    return true;
                })
                .verifyComplete();

        verify(cartService).get(userUuid);
        verify(orderRepository).save(orderDao);
        verify(orderItemRepository).saveAll(anyList());
        verify(cartService).clear(userUuid);
    }

    @Test
    void getUserOrders_ShouldReturnOrdersWithItems() {
        UUID orderUuid = UUID.randomUUID();
        OrderDao orderDao = OrderDao.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(cartUuid)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("20.00"))
                .createdAt(LocalDateTime.now())
                .build();

        OrderItemDao itemDao = OrderItemDao.builder()
                .uuid(UUID.randomUUID())
                .orderUuid(orderUuid)
                .productUuid(UUID.randomUUID())
                .quantity(2)
                .priceAtOrder(new BigDecimal("10.00"))
                .build();

        OrderItem item = new OrderItem(
                itemDao.getUuid(), orderUuid, itemDao.getProductUuid(), 2, itemDao.getPriceAtOrder()
        );

        Order order = Order.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(cartUuid)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("20.00"))
                .createdAt(LocalDateTime.now())
                .items(List.of(item))
                .build();

        when(orderRepository.findByUserUuid(userUuid)).thenReturn(Flux.just(orderDao));
        when(orderItemRepository.findByOrderUuid(orderUuid)).thenReturn(Flux.just(itemDao));
        when(orderMapper.orderDaoToOrder(orderDao)).thenReturn(order);
        when(orderItemMapper.orderItemDaoToOrderItem(itemDao)).thenReturn(item);

        StepVerifier.create(orderService.getUserOrders(userUuid))
                .expectNextMatches(result -> {
                    assertEquals(orderUuid, result.getUuid());
                    assertEquals(1, result.getItems().size());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void getByUuid_ShouldReturnOrder_WhenExists() {
        UUID orderUuid = UUID.randomUUID();

        OrderDao orderDao = OrderDao.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(cartUuid)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("20.00"))
                .createdAt(LocalDateTime.now())
                .build();

        OrderItemDao itemDao = OrderItemDao.builder()
                .uuid(UUID.randomUUID())
                .orderUuid(orderUuid)
                .productUuid(UUID.randomUUID())
                .quantity(2)
                .priceAtOrder(new BigDecimal("10.00"))
                .build();

        OrderItem item = new OrderItem(
                itemDao.getUuid(), orderUuid, itemDao.getProductUuid(), 2, itemDao.getPriceAtOrder()
        );

        Order order = Order.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(cartUuid)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("20.00"))
                .createdAt(LocalDateTime.now())
                .items(List.of(item))
                .build();

        when(orderRepository.findByUuidAndUserUuid(orderUuid, userUuid)).thenReturn(Mono.just(orderDao));
        when(orderItemRepository.findByOrderUuid(orderUuid)).thenReturn(Flux.just(itemDao));
        when(orderMapper.orderDaoToOrder(orderDao)).thenReturn(order);
        when(orderItemMapper.orderItemDaoToOrderItem(itemDao)).thenReturn(item);

        StepVerifier.create(orderService.getByUuid(userUuid, orderUuid))
                .expectNextMatches(result -> {
                    assertEquals(orderUuid, result.getUuid());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void getByUuid_ShouldThrow_WhenOrderNotFound() {
        UUID orderUuid = UUID.randomUUID();
        when(orderRepository.findByUuidAndUserUuid(orderUuid, userUuid)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getByUuid(userUuid, orderUuid))
                .expectErrorMatches(e -> e instanceof OrderNotFoundException &&
                        e.getMessage().contains("Заказ не найден"))
                .verify();
    }

    @Test
    void checkout_ShouldUpdateStatusToPaid() {
        UUID orderUuid = UUID.randomUUID();

        OrderDao orderDao = OrderDao.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .status(OrderStatus.CREATED)
                .build();

        when(orderRepository.findByUuidAndUserUuid(orderUuid, userUuid)).thenReturn(Mono.just(orderDao));
        when(orderRepository.save(any(OrderDao.class))).thenReturn(Mono.just(orderDao));

        StepVerifier.create(orderService.checkout(userUuid, orderUuid))
                .verifyComplete();

        assertEquals(OrderStatus.PAID, orderDao.getStatus());
    }

    @Test
    void cancel_ShouldUpdateStatusToCancelled() {
        UUID orderUuid = UUID.randomUUID();

        OrderDao orderDao = OrderDao.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .status(OrderStatus.CREATED)
                .build();

        when(orderRepository.findByUuidAndUserUuid(orderUuid, userUuid)).thenReturn(Mono.just(orderDao));
        when(orderRepository.save(any(OrderDao.class))).thenReturn(Mono.just(orderDao));

        StepVerifier.create(orderService.cancel(userUuid, orderUuid))
                .verifyComplete();

        assertEquals(OrderStatus.CANCELLED, orderDao.getStatus());
    }

    @Test
    void checkout_ShouldFailForInvalidTransition() {
        UUID orderUuid = UUID.randomUUID();

        OrderDao orderDao = OrderDao.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .status(OrderStatus.DELIVERED) // Нельзя перейти в PAID
                .build();

        when(orderRepository.findByUuidAndUserUuid(orderUuid, userUuid)).thenReturn(Mono.just(orderDao));

        StepVerifier.create(orderService.checkout(userUuid, orderUuid))
                .expectErrorMatches(e -> e instanceof IllegalOrderStateException &&
                        e.getMessage().contains("Недопустимый переход"))
                .verify();
    }

    @Test
    void getUserTotalAmount_ShouldReturnCorrectAmount() {
        BigDecimal total = new BigDecimal("123.45");
        when(orderRepository.getTotalOrdersAmountByUser(userUuid)).thenReturn(Mono.just(total));

        StepVerifier.create(orderService.getUserTotalAmount(userUuid))
                .expectNext(total)
                .verifyComplete();
    }

}