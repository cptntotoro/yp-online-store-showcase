package ru.practicum.repository.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.user.User;
import ru.practicum.repository.user.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID testUserUuid;
    private UUID secondUserUuid;

    @BeforeEach
    void setUp() {
        User user1 = new User();
        user1.setUsername("testuser1");
        user1.setEmail("test1@example.com");
        user1 = userRepository.save(user1);
        testUserUuid = user1.getUuid();

        User user2 = new User();
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        user2 = userRepository.save(user2);
        secondUserUuid = user2.getUuid();
    }

    @Test
    void findByUserUuid_shouldReturnEmptyOptional_whenCartNotFound() {
        UUID nonExistingUserUuid = UUID.randomUUID();
        Optional<Cart> result = cartRepository.findByUserUuid(nonExistingUserUuid);
        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserUuid_shouldReturnCart_whenCartExists() {
        Cart cart = new Cart();
        cart.setUserUuid(testUserUuid);
        cartRepository.save(cart);

        Optional<Cart> result = cartRepository.findByUserUuid(testUserUuid);

        assertTrue(result.isPresent());
        assertEquals(testUserUuid, result.get().getUserUuid());
    }

    @Test
    void findByUserUuid_shouldReturnCorrectCart_whenMultipleCartsExist() {
        Cart cart1 = new Cart();
        cart1.setUserUuid(testUserUuid);
        cartRepository.save(cart1);

        Cart cart2 = new Cart();
        cart2.setUserUuid(secondUserUuid);
        cartRepository.save(cart2);

        Optional<Cart> result1 = cartRepository.findByUserUuid(testUserUuid);
        Optional<Cart> result2 = cartRepository.findByUserUuid(secondUserUuid);

        assertAll(
                () -> assertTrue(result1.isPresent()),
                () -> assertEquals(testUserUuid, result1.get().getUserUuid()),
                () -> assertTrue(result2.isPresent()),
                () -> assertEquals(secondUserUuid, result2.get().getUserUuid())
        );
    }
}