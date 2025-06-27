package ru.practicum.service.cart;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.practicum.dao.product.ProductDao;
import ru.practicum.dao.user.UserDao;
import ru.practicum.dto.cart.cache.CartCacheDto;
import ru.practicum.exception.cart.IllegalCartStateException;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.product.Product;
import ru.practicum.repository.cart.CartItemRepository;
import ru.practicum.repository.cart.CartRepository;
import ru.practicum.repository.product.ProductRepository;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.service.CacheServiceIntegrationTest;
import ru.practicum.service.product.ProductService;
import ru.practicum.service.user.UserService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class CartServiceIntegrationTest extends CacheServiceIntegrationTest {

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartCacheServiceImpl cartCacheService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ReactiveRedisTemplate<String, CartCacheDto> cartCacheTemplate;

    private UUID userId;
    private Product testProduct;
    private UUID productId;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll().block();
        cartRepository.deleteAll().block();
        productRepository.deleteAll().block();
        userRepository.deleteAll().block();
        cartCacheTemplate.getConnectionFactory().getReactiveConnection().serverCommands().flushAll().block();

        UserDao userDao = new UserDao();
        userDao.setUsername("test_user");
        userDao.setCreatedAt(LocalDateTime.now());
        userDao = userRepository.save(userDao).block();
        userId = Objects.requireNonNull(userDao).getUuid();

        ProductDao productDao = new ProductDao();
        productDao.setName("Test Product");
        productDao.setPrice(BigDecimal.valueOf(100.0));
        productDao = productRepository.save(productDao).block();
        productId = Objects.requireNonNull(productDao).getUuid();

        testProduct = new Product();
        testProduct.setUuid(productId);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(100.0));

        testCart = cartService.createGuest(userId).block();
    }

    @AfterEach
    void tearDown() {
        cartItemRepository.deleteAll().block();
        cartRepository.deleteAll().block();
        productRepository.deleteAll().block();
        userRepository.deleteAll().block();
        cartCacheTemplate.getConnectionFactory().getReactiveConnection().serverCommands().flushAll().block();
    }

    @Test
    void createGuest_ShouldCreateNewCart() {
        UserDao userDao = new UserDao();
        userDao.setUsername("test_user");
        userDao.setCreatedAt(LocalDateTime.now());
        userDao = userRepository.save(userDao).block();
        UUID newUserId = Objects.requireNonNull(userDao).getUuid();

        StepVerifier.create(cartService.createGuest(newUserId))
                .assertNext(cart -> {
                    assertNotNull(cart.getUuid());
                    assertEquals(newUserId, cart.getUserUuid());
                    assertEquals(0, BigDecimal.ZERO.compareTo(cart.getTotalPrice()));
                    assertTrue(cart.getItems().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void get_WhenCartExists_ShouldReturnCartWithItems() {
        cartService.addToCart(userId, productId, 1).block();

        StepVerifier.create(cartService.get(userId))
                .assertNext(cart -> {
                    assertEquals(testCart.getUuid(), cart.getUuid());
                    assertEquals(userId, cart.getUserUuid());
                    assertEquals(1, cart.getItems().size());
                    assertEquals(productId, cart.getItems().getFirst().getProduct().getUuid());
                    assertEquals(0, BigDecimal.valueOf(100.0).compareTo(cart.getTotalPrice()));
                })
                .verifyComplete();
    }

    @Test
    void addToCart_ShouldAddProductToCart() {
        int quantity = 2;

        StepVerifier.create(cartService.addToCart(userId, productId, quantity))
                .assertNext(cart -> {
                    assertEquals(1, cart.getItems().size());
                    CartItem item = cart.getItems().getFirst();
                    assertEquals(productId, item.getProduct().getUuid());
                    assertEquals(quantity, item.getQuantity());
                    assertEquals(0, testProduct.getPrice()
                            .multiply(BigDecimal.valueOf(quantity))
                            .compareTo(cart.getTotalPrice()));
                })
                .verifyComplete();
    }

    @Test
    void addToCart_WhenInvalidQuantity_ShouldThrowException() {
        StepVerifier.create(cartService.addToCart(userId, productId, 0))
                .expectError(IllegalCartStateException.class)
                .verify();
    }

    @Test
    void removeFromCart_ShouldRemoveProductFromCart() {
        cartService.addToCart(userId, productId, 1).block();

        StepVerifier.create(cartService.removeFromCart(userId, productId))
                .assertNext(cart -> {
                    assertTrue(cart.getItems().isEmpty());
                    assertEquals(BigDecimal.ZERO, cart.getTotalPrice());
                })
                .verifyComplete();
    }

    @Test
    void updateQuantity_ShouldUpdateProductQuantity() {
        int initialQuantity = 1;
        int updatedQuantity = 3;

        cartService.addToCart(userId, productId, initialQuantity).block();

        StepVerifier.create(cartService.updateQuantity(userId, productId, updatedQuantity))
                .assertNext(cart -> {
                    assertEquals(1, cart.getItems().size());
                    CartItem item = cart.getItems().getFirst();
                    assertEquals(productId, item.getProduct().getUuid());
                    assertEquals(updatedQuantity, item.getQuantity());
                    assertEquals(0, testProduct.getPrice()
                            .multiply(BigDecimal.valueOf(updatedQuantity))
                            .compareTo(cart.getTotalPrice()));
                })
                .verifyComplete();
    }

    @Test
    void clear_ShouldRemoveAllItemsFromCart() {
        cartService.addToCart(userId, productId, 1).block();

        StepVerifier.create(cartService.clear(userId))
                .verifyComplete();

        StepVerifier.create(cartService.get(userId))
                .assertNext(cart -> {
                    assertTrue(cart.getItems().isEmpty());
                    assertEquals(0, BigDecimal.ZERO.compareTo(cart.getTotalPrice()));
                })
                .verifyComplete();
    }

    @Test
    void get_ShouldCacheCart() {
        cartCacheService.evict(userId).block();

        Cart cartWithItem = cartService.addToCart(userId, productId, 1).block();
        assertNotNull(cartWithItem);
        assertEquals(1, cartWithItem.getItems().size());

        StepVerifier.create(cartService.get(userId))
                .assertNext(cart -> {
                    assertEquals(1, cart.getItems().size());
                    assertEquals(productId, cart.getItems().getFirst().getProduct().getUuid());
                })
                .verifyComplete();

        Cart cachedCart = cartCacheService.getCart(userId).block();
        assertNotNull(cachedCart);
        assertEquals(1, cachedCart.getItems().size());

        cartItemRepository.deleteByCartUuid(testCart.getUuid()).block();
        cartRepository.deleteById(testCart.getUuid()).block();

        assertNotEquals(Boolean.TRUE, cartRepository.existsById(testCart.getUuid()).block());
        assertTrue(Objects.requireNonNull(cartItemRepository.findByCartUuid(testCart.getUuid()).collectList().block()).isEmpty());

        StepVerifier.create(cartService.get(userId))
                .assertNext(cachedCartFromService -> {
                    assertEquals(1, cachedCartFromService.getItems().size());
                    assertEquals(productId, cachedCartFromService.getItems().getFirst().getProduct().getUuid());
                })
                .verifyComplete();
    }
}