package ru.practicum.repository.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.config.TestR2dbcConfiguration;
import ru.practicum.dao.cart.CartDao;
import ru.practicum.dao.order.OrderDao;
import ru.practicum.dao.user.UserDao;
import ru.practicum.model.order.OrderStatus;
import ru.practicum.repository.cart.CartRepository;
import ru.practicum.repository.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@DataR2dbcTest
@Import(TestR2dbcConfiguration.class)
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll().block();
        cartRepository.deleteAll().block();
        userRepository.deleteAll().block();
    }

    private Mono<OrderDao> createTestOrder(UUID userUuid, UUID cartUuid) {
        OrderDao order = new OrderDao();
        order.setUserUuid(userUuid);
        order.setCartUuid(cartUuid);
        order.setStatus(OrderStatus.CREATED);
        order.setTotalPrice(new BigDecimal("100.00"));
        order.setCreatedAt(LocalDateTime.now());
        return orderRepository.save(order);
    }

    private Mono<UserDao> createTestUser() {
        UserDao user = new UserDao();
        user.setUsername(UUID.randomUUID().toString());
        user.setEmail(UUID.randomUUID() + "@example.com");
        return userRepository.save(user);
    }

    private Mono<CartDao> createTestCart(UUID userUuid) {
        CartDao cart = new CartDao();
        cart.setUserUuid(userUuid);
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    @Test
    void findByUserUuid_shouldReturnUserOrders() {
        Flux<OrderDao> testFlow = createTestUser()
                .flatMap(user -> createTestCart(user.getUuid()))
                .flatMapMany(cart -> {
                    OrderDao order1 = new OrderDao();
                    order1.setUserUuid(cart.getUserUuid());
                    order1.setCartUuid(cart.getUuid());
                    order1.setStatus(OrderStatus.CREATED);
                    order1.setTotalPrice(new BigDecimal("50.00"));
                    order1.setCreatedAt(LocalDateTime.now());

                    OrderDao order2 = new OrderDao();
                    order2.setUserUuid(cart.getUserUuid());
                    order2.setCartUuid(cart.getUuid());
                    order2.setStatus(OrderStatus.DELIVERED);
                    order2.setTotalPrice(new BigDecimal("150.00"));
                    order2.setCreatedAt(LocalDateTime.now());

                    return Flux.merge(
                            orderRepository.save(order1),
                            orderRepository.save(order2)
                    ).thenMany(orderRepository.findByUserUuid(cart.getUserUuid()));
                });

        StepVerifier.create(testFlow)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void findByUuidAndUserUuid_shouldReturnOrderWhenBelongsToUser() {
        Mono<OrderDao> testFlow = createTestUser()
                .flatMap(user -> createTestCart(user.getUuid()))
                .flatMap(cart -> createTestOrder(cart.getUserUuid(), cart.getUuid()))
                .flatMap(order -> orderRepository.findByUuidAndUserUuid(
                        order.getUuid(),
                        order.getUserUuid()));

        StepVerifier.create(testFlow)
                .expectNextMatches(order ->
                        order.getStatus() == OrderStatus.CREATED)
                .verifyComplete();
    }

    @Test
    void findByUuidAndUserUuid_shouldReturnEmptyWhenNotBelongsToUser() {
        Mono<OrderDao> testFlow = createTestUser()
                .flatMap(user -> createTestCart(user.getUuid()))
                .flatMap(cart -> createTestOrder(cart.getUserUuid(), cart.getUuid()))
                .flatMap(order -> {
                    UUID wrongUserUuid = UUID.randomUUID();
                    return orderRepository.findByUuidAndUserUuid(
                            order.getUuid(),
                            wrongUserUuid);
                });

        StepVerifier.create(testFlow)
                .verifyComplete();
    }

    @Test
    void getTotalOrdersAmountByUser_shouldReturnCorrectSum() {
        Mono<BigDecimal> testFlow = createTestUser()
                .flatMap(user -> createTestCart(user.getUuid()))
                .flatMap(cart -> {
                    OrderDao order1 = new OrderDao();
                    order1.setUserUuid(cart.getUserUuid());
                    order1.setCartUuid(cart.getUuid());
                    order1.setStatus(OrderStatus.CREATED);
                    order1.setTotalPrice(new BigDecimal("100.00"));

                    OrderDao order2 = new OrderDao();
                    order2.setUserUuid(cart.getUserUuid());
                    order2.setCartUuid(cart.getUuid());
                    order2.setStatus(OrderStatus.DELIVERED);
                    order2.setTotalPrice(new BigDecimal("200.00"));

                    return orderRepository.save(order1)
                            .then(orderRepository.save(order2))
                            .thenReturn(cart.getUserUuid());
                })
                .flatMap(orderRepository::getTotalOrdersAmountByUser);

        StepVerifier.create(testFlow)
                .expectNextMatches(total ->
                        total.compareTo(new BigDecimal("300.00")) == 0)
                .verifyComplete();
    }

    @Test
    void getTotalOrdersAmountByUser_shouldReturnZeroWhenNoOrders() {
        Mono<BigDecimal> testFlow = createTestUser()
                .map(UserDao::getUuid)
                .flatMap(orderRepository::getTotalOrdersAmountByUser);

        StepVerifier.create(testFlow)
                .expectNextMatches(total ->
                        total.compareTo(BigDecimal.ZERO) == 0)
                .verifyComplete();
    }
}