package ru.practicum.service.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.order.OrderDao;
import ru.practicum.exception.cart.IllegalCartStateException;
import ru.practicum.exception.order.OrderNotFoundException;
import ru.practicum.mapper.order.OrderItemMapper;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.order.*;
import ru.practicum.model.product.Product;
import ru.practicum.repository.order.OrderItemRepository;
import ru.practicum.repository.order.OrderRepository;
import ru.practicum.service.cart.CartService;
import ru.practicum.service.product.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Mock
    private ProductService productService;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void create_WithEmptyCart_ShouldThrowException() {
        UUID userUuid = UUID.randomUUID();
        Cart emptyCart = Cart.builder()
                .uuid(userUuid)
                .items(Collections.emptyList())
                .build();

        when(cartService.get(userUuid)).thenReturn(Mono.just(emptyCart));

        StepVerifier.create(orderService.create(userUuid))
                .expectError(IllegalCartStateException.class)
                .verify();

        verify(cartService).get(userUuid);
        verifyNoMoreInteractions(cartService, orderRepository, orderItemRepository);
    }

    @Test
    void create_WithValidCart_ShouldCreateOrder() {
        UUID userUuid = UUID.randomUUID();
        UUID productUuid = UUID.randomUUID();

        Product product = Product.builder()
                .uuid(productUuid)
                .name("Product")
                .price(BigDecimal.TEN)
                .description("Description")
                .build();

        CartItem cartItem = CartItem.builder()
                .product(product)
                .quantity(2)
                .build();

        Cart cart = Cart.builder()
                .uuid(userUuid)
                .items(List.of(cartItem))
                .build();

        OrderDao savedOrderDao = OrderDao.builder()
                .uuid(UUID.randomUUID())
                .userUuid(userUuid)
                .cartUuid(userUuid)
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.valueOf(20))
                .createdAt(LocalDateTime.now())
                .build();

        OrderItem orderItem = OrderItem.builder()
                .orderUuid(savedOrderDao.getUuid())
                .productUuid(productUuid)
                .quantity(2)
                .priceAtOrder(BigDecimal.TEN)
                .build();

        Order expectedOrder = Order.builder()
                .uuid(savedOrderDao.getUuid())
                .userUuid(userUuid)
                .cartUuid(userUuid)
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.valueOf(20))
                .createdAt(LocalDateTime.now())
                .items(List.of(orderItem))
                .build();

        when(cartService.get(userUuid)).thenReturn(Mono.just(cart));
        when(orderRepository.save(any(OrderDao.class))).thenReturn(Mono.just(savedOrderDao));
        when(orderMapper.orderToOrderDao(any(Order.class))).thenReturn(savedOrderDao);
        when(orderMapper.orderDaoToOrderWithItems(any(OrderDao.class), anyList())).thenReturn(expectedOrder);

        when(orderItemRepository.saveAll(anyList())).thenReturn(Flux.empty());

        when(cartService.clear(userUuid)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.create(userUuid))
                .expectNext(expectedOrder)
                .verifyComplete();

        verify(cartService).get(userUuid);
        verify(orderRepository).save(any(OrderDao.class));
        verify(orderItemRepository).saveAll(anyList());
        verify(cartService).clear(userUuid);
    }

    @Test
    void getUserOrders_ShouldReturnOrders() {
        UUID userUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();

        OrderDao orderDao = OrderDao.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(UUID.randomUUID())
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .build();

        Order expectedOrder = Order.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(UUID.randomUUID())
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.findByUserUuid(userUuid)).thenReturn(Flux.just(orderDao));
        when(orderMapper.orderDaoToOrder(any())).thenReturn(expectedOrder);
        when(orderItemRepository.findByOrderUuid(orderUuid)).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getUserOrders(userUuid))
                .expectNext(expectedOrder)
                .verifyComplete();

        verify(orderRepository).findByUserUuid(userUuid);
    }

    @Test
    void getByUuid_WhenOrderExists_ShouldReturnOrder() {
        UUID userUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();

        OrderDao orderDao = OrderDao.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(UUID.randomUUID())
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .build();

        Order expectedOrder = Order.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(UUID.randomUUID())
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.findByUuidAndUserUuid(orderUuid, userUuid)).thenReturn(Mono.just(orderDao));
        when(orderMapper.orderDaoToOrder(any())).thenReturn(expectedOrder);
        when(orderItemRepository.findByOrderUuid(orderUuid)).thenReturn(Flux.empty());

        StepVerifier.create(orderService.getByUuid(userUuid, orderUuid))
                .expectNext(expectedOrder)
                .verifyComplete();

        verify(orderRepository).findByUuidAndUserUuid(orderUuid, userUuid);
    }

    @Test
    void getByUuid_WhenOrderNotExists_ShouldThrowException() {
        UUID userUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();

        when(orderRepository.findByUuidAndUserUuid(orderUuid, userUuid)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getByUuid(userUuid, orderUuid))
                .expectError(OrderNotFoundException.class)
                .verify();

        verify(orderRepository).findByUuidAndUserUuid(orderUuid, userUuid);
    }

    @Test
    void checkout_WithValidOrder_ShouldUpdateStatus() {
        UUID userUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();

        OrderDao orderDao = OrderDao.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(UUID.randomUUID())
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.findByUuidAndUserUuid(orderUuid, userUuid)).thenReturn(Mono.just(orderDao));
        when(orderRepository.save(any())).thenReturn(Mono.just(orderDao));

        StepVerifier.create(orderService.checkout(userUuid, orderUuid))
                .verifyComplete();

        verify(orderRepository).findByUuidAndUserUuid(orderUuid, userUuid);
        verify(orderRepository).save(any());
    }

    @Test
    void cancel_WithValidOrder_ShouldUpdateStatus() {
        UUID userUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();

        OrderDao orderDao = OrderDao.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(UUID.randomUUID())
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.findByUuidAndUserUuid(orderUuid, userUuid)).thenReturn(Mono.just(orderDao));
        when(orderRepository.save(any())).thenReturn(Mono.just(orderDao));

        StepVerifier.create(orderService.cancel(userUuid, orderUuid))
                .verifyComplete();

        verify(orderRepository).findByUuidAndUserUuid(orderUuid, userUuid);
        verify(orderRepository).save(any());
    }

    @Test
    void getUserTotalAmount_ShouldReturnTotal() {
        UUID userUuid = UUID.randomUUID();
        BigDecimal expectedTotal = BigDecimal.valueOf(100);

        when(orderRepository.getTotalOrdersAmountByUser(userUuid)).thenReturn(Mono.just(expectedTotal));

        StepVerifier.create(orderService.getUserTotalAmount(userUuid))
                .expectNext(expectedTotal)
                .verifyComplete();

        verify(orderRepository).getTotalOrdersAmountByUser(userUuid);
    }

    @Test
    void getUserOrdersWithProducts_ShouldReturnOrdersWithProducts() {
        UUID userUuid = UUID.randomUUID();
        UUID orderUuid = UUID.randomUUID();
        UUID productUuid = UUID.randomUUID();

        OrderDao orderDao = OrderDao.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(UUID.randomUUID())
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .build();

        Order order = Order.builder()
                .uuid(orderUuid)
                .userUuid(userUuid)
                .cartUuid(UUID.randomUUID())
                .status(OrderStatus.CREATED)
                .totalPrice(BigDecimal.TEN)
                .createdAt(LocalDateTime.now())
                .items(List.of(
                        OrderItem.builder()
                                .orderUuid(orderUuid)
                                .productUuid(productUuid)
                                .quantity(1)
                                .priceAtOrder(BigDecimal.TEN)
                                .build()
                ))
                .build();

        Product product = Product.builder()
                .uuid(productUuid)
                .name("Product")
                .price(BigDecimal.TEN)
                .description("Description")
                .build();

        BigDecimal totalAmount = BigDecimal.TEN;

        when(orderRepository.findByUserUuid(userUuid)).thenReturn(Flux.just(orderDao));
        when(orderMapper.orderDaoToOrder(any())).thenReturn(order);
        when(orderItemRepository.findByOrderUuid(orderUuid)).thenReturn(Flux.empty());
        when(productService.getProductsByIds(any())).thenReturn(Mono.just(Map.of(productUuid, product)));
        when(orderRepository.getTotalOrdersAmountByUser(userUuid)).thenReturn(Mono.just(totalAmount));

        StepVerifier.create(orderService.getUserOrdersWithProducts(userUuid))
                .assertNext(result -> {
                    assertThat(result.getOrders()).hasSize(1);
                    assertThat(result.getProducts()).hasSize(1);
                    assertThat(result.getTotalAmount()).isEqualTo(totalAmount);
                })
                .verifyComplete();

        verify(orderRepository).findByUserUuid(userUuid);
        verify(orderRepository).getTotalOrdersAmountByUser(userUuid);
        verify(productService).getProductsByIds(any());
    }
}