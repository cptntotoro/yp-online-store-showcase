package ru.practicum.repository.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.cart.CartDao;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@DataR2dbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    private UUID userUuid1;
    private UUID userUuid2;

    @BeforeEach
    void setUp() {
        userUuid1 = UUID.randomUUID();
        userUuid2 = UUID.randomUUID();
    }

    @Test
    @DisplayName("findByUserUuid should return empty when cart does not exist")
    void findByUserUuid_shouldReturnEmpty_whenCartNotFound() {
        UUID nonExistingUuid = UUID.randomUUID();

        StepVerifier.create(cartRepository.findByUserUuid(nonExistingUuid))
                .verifyComplete();
    }

    @Test
    @DisplayName("findByUserUuid should return cart when it exists")
    void findByUserUuid_shouldReturnCart_whenExists() {
        CartDao cart = new CartDao();
        cart.setUuid(UUID.randomUUID());
        cart.setUserUuid(userUuid1);
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setCreatedAt(LocalDateTime.now());
        cart.setUpdatedAt(LocalDateTime.now());

        Mono<CartDao> saveAndFind = cartRepository.save(cart)
                .then(cartRepository.findByUserUuid(userUuid1));

        StepVerifier.create(saveAndFind)
                .expectNextMatches(found ->
                        found.getUserUuid().equals(userUuid1) &&
                                found.getTotalPrice().compareTo(BigDecimal.ZERO) == 0)
                .verifyComplete();
    }

    @Test
    @DisplayName("findByUserUuid should return correct cart when multiple carts exist")
    void findByUserUuid_shouldReturnCorrectCart_whenMultipleCartsExist() {
        CartDao cart1 = new CartDao(UUID.randomUUID(), userUuid1, BigDecimal.ONE, LocalDateTime.now(), LocalDateTime.now());
        CartDao cart2 = new CartDao(UUID.randomUUID(), userUuid2, BigDecimal.TEN, LocalDateTime.now(), LocalDateTime.now());

        Mono<CartDao> findUser1 = cartRepository.save(cart1)
                .then(cartRepository.save(cart2))
                .then(cartRepository.findByUserUuid(userUuid1));

        Mono<CartDao> findUser2 = cartRepository.findByUserUuid(userUuid2);

        StepVerifier.create(findUser1)
                .expectNextMatches(cart -> cart.getUserUuid().equals(userUuid1))
                .verifyComplete();

        StepVerifier.create(findUser2)
                .expectNextMatches(cart -> cart.getUserUuid().equals(userUuid2))
                .verifyComplete();
    }
}