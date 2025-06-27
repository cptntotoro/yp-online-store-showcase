package ru.practicum.repository.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.config.TestR2dbcConfiguration;
import ru.practicum.dao.cart.CartDao;
import ru.practicum.dao.user.UserDao;
import ru.practicum.repository.order.OrderItemRepository;
import ru.practicum.repository.order.OrderRepository;
import ru.practicum.repository.user.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@DataR2dbcTest
@Import(TestR2dbcConfiguration.class)
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll().block();
        orderRepository.deleteAll().block();
        cartItemRepository.deleteAll().block();
        cartRepository.deleteAll().block();
        userRepository.deleteAll().block();
    }

    @Test
    void findByUserUuid_shouldReturnEmpty_whenCartNotFound() {
        UUID nonExistingUuid = UUID.randomUUID();

        StepVerifier.create(cartRepository.findByUserUuid(nonExistingUuid))
                .verifyComplete();
    }

    @Test
    void findByUserUuid_shouldReturnCart_whenExists() {
        UserDao user = new UserDao();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        Mono<CartDao> testFlow = userRepository.save(user)
                .flatMap(savedUser -> {
                    CartDao cart = new CartDao();
                    cart.setUserUuid(savedUser.getUuid());
                    cart.setTotalPrice(BigDecimal.ZERO);
                    cart.setCreatedAt(LocalDateTime.now());
                    cart.setUpdatedAt(LocalDateTime.now());
                    return cartRepository.save(cart);
                })
                .flatMap(savedCart -> cartRepository.findByUserUuid(savedCart.getUserUuid()));

        StepVerifier.create(testFlow)
                .expectNextMatches(found ->
                        found.getTotalPrice().compareTo(BigDecimal.ZERO) == 0)
                .verifyComplete();
    }

    @Test
    void findByUserUuid_shouldReturnCorrectCart_whenMultipleCartsExist() {
        UserDao user1 = new UserDao();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");

        UserDao user2 = new UserDao();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        Mono<CartDao> testFlow = Mono.zip(userRepository.save(user1), userRepository.save(user2))
                .flatMap(tuple -> {
                    UserDao savedUser1 = tuple.getT1();
                    UserDao savedUser2 = tuple.getT2();

                    CartDao cart1 = new CartDao();
                    cart1.setUserUuid(savedUser1.getUuid());
                    cart1.setTotalPrice(BigDecimal.ONE);
                    cart1.setCreatedAt(LocalDateTime.now());

                    CartDao cart2 = new CartDao();
                    cart2.setUserUuid(savedUser2.getUuid());
                    cart2.setTotalPrice(BigDecimal.TEN);
                    cart2.setCreatedAt(LocalDateTime.now());

                    return cartRepository.save(cart1)
                            .then(cartRepository.save(cart2))
                            .then(cartRepository.findByUserUuid(savedUser1.getUuid()));
                });

        StepVerifier.create(testFlow)
                .expectNextMatches(cart ->
                        cart.getTotalPrice().compareTo(BigDecimal.ONE) == 0)
                .verifyComplete();
    }
}