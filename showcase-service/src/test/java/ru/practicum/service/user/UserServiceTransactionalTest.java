package ru.practicum.service.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.ReactiveTransactionManager;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.model.cart.Cart;
import ru.practicum.repository.cart.CartRepository;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.service.cart.CartService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@DirtiesContext
class UserServiceTransactionalTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ReactiveTransactionManager transactionManager;

    @MockBean
    private CartService cartService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll().block();
        cartRepository.deleteAll().block();
    }

    @Test
    void createGuest_shouldRollbackUserWhenCartFails() {
        when(cartService.createGuest(any(UUID.class)))
                .thenReturn(Mono.error(new RuntimeException("Cart creation failed")));

        StepVerifier.create(userService.createGuest())
                .expectErrorMatches(e -> e.getMessage().contains("Ошибка создания гостевого пользователя и его корзины"))
                .verify();

        StepVerifier.create(userRepository.count())
                .expectNext(0L)
                .verifyComplete();
    }

    @Test
    void createGuest_shouldCommitWhenBothOperationsSucceed() {
        when(cartService.createGuest(any(UUID.class)))
                .thenAnswer(invocation -> {
                    UUID userUuid = invocation.getArgument(0);
                    Cart cart = Cart.builder()
                            .uuid(UUID.randomUUID())
                            .userUuid(userUuid)
                            .items(new ArrayList<>())
                            .totalPrice(BigDecimal.ZERO)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return Mono.just(cart);
                });

        StepVerifier.create(userService.createGuest())
                .assertNext(user -> {
                    assertNotNull(user.getUuid());
                    assertEquals("guest", user.getUsername());
                })
                .verifyComplete();

        StepVerifier.create(userRepository.count())
                .expectNext(1L)
                .verifyComplete();
    }
}