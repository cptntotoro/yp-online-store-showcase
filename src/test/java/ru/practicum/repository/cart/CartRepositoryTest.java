package ru.practicum.repository.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.product.Product;
import ru.practicum.model.user.User;
import ru.practicum.repository.product.ProductRepository;
import ru.practicum.repository.user.UserRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;
    private Product testProduct;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findAll().getFirst();
        testProduct = productRepository.findAll().getFirst();
        testCart = cartRepository.findAll().getFirst();
    }

    @Test
    void findByUserUuid_shouldReturnCart_whenCartExists() {
        Optional<Cart> foundCart = cartRepository.findByUserUuid(testUser.getUuid());

        assertThat(foundCart).isPresent();
        assertThat(foundCart.get().getUserUuid()).isEqualTo(testUser.getUuid());
    }

    @Test
    void findByUserUuid_shouldReturnEmptyOptional_whenCartNotExists() {
        UUID nonExistingUserUuid = UUID.randomUUID();

        Optional<Cart> foundCart = cartRepository.findByUserUuid(nonExistingUserUuid);

        assertThat(foundCart).isEmpty();
    }
}