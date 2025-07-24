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
        OrderDao order = OrderDao.builder()
                .userUuid(userUuid)
                .cartUuid(cartUuid)
                .status(OrderStatus.CREATED)
                .totalPrice(new BigDecimal("100.00"))
                .createdAt(LocalDateTime.now())
                .build();
        return orderRepository.save(order);
    }

    private Mono<UserDao> createTestUser() {
        UserDao user = UserDao.builder()
                .username(UUID.randomUUID().toString())
                .email(UUID.randomUUID() + "@example.com")
                .password(UUID.randomUUID().toString())
                .build();
        return userRepository.save(user);
    }

    private Mono<CartDao> createTestCart(UUID userUuid) {
        CartDao cart = CartDao.builder()
                .userUuid(userUuid)
                .totalPrice(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return cartRepository.save(cart);
    }

    @Test
    void findByUserUuid_shouldReturnUserOrders() {
        Flux<OrderDao> testFlow = createTestUser()
                .flatMap(user -> createTestCart(user.getUuid()))
                .flatMapMany(cart -> {
                    OrderDao order1 = OrderDao.builder()
                            .userUuid(cart.getUserUuid())
                            .cartUuid(cart.getUuid())
                            .status(OrderStatus.CREATED)
                            .totalPrice(new BigDecimal("50.00"))
                            .createdAt(LocalDateTime.now())
                            .build();

                    OrderDao order2 = OrderDao.builder()
                            .userUuid(cart.getUserUuid())
                            .cartUuid(cart.getUuid())
                            .status(OrderStatus.DELIVERED)
                            .totalPrice(new BigDecimal("150.00"))
                            .createdAt(LocalDateTime.now())
                            .build();

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
                    OrderDao order1 = OrderDao.builder()
                            .userUuid(cart.getUserUuid())
                            .cartUuid(cart.getUuid())
                            .status(OrderStatus.CREATED)
                            .totalPrice(new BigDecimal("100.00"))
                            .build();

                    OrderDao order2 = OrderDao.builder()
                            .userUuid(cart.getUserUuid())
                            .cartUuid(cart.getUuid())
                            .status(OrderStatus.DELIVERED)
                            .totalPrice(new BigDecimal("200.00"))
                            .build();

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