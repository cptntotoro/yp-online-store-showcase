package ru.practicum.service.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.cart.CartDao;
import ru.practicum.dao.cart.CartItemDao;
import ru.practicum.dao.product.ProductDao;
import ru.practicum.dao.user.UserDao;
import ru.practicum.exception.cart.IllegalCartStateException;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.product.Product;
import ru.practicum.repository.cart.CartItemRepository;
import ru.practicum.repository.cart.CartRepository;
import ru.practicum.repository.product.ProductRepository;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.service.product.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CartServiceTransactionalTest {

    @Autowired
    private CartServiceImpl cartService;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private ProductService productService;

    @MockBean
    private CartCacheService cartCacheService;

    private UUID userId;
    private UUID productId;
    private UUID cartId;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        cartItemRepository.deleteAll().block();
        cartRepository.deleteAll().block();
        userRepository.deleteAll().block();
        productRepository.deleteAll().block();

        UserDao user = userRepository.save(UserDao.builder()
                .username("test_user")
                .email("test@example.com")
                .password("password")
                .createdAt(LocalDateTime.now())
                .build()).block();
        userId = Objects.requireNonNull(user).getUuid();

        CartDao cart = cartRepository.save(CartDao.builder()
                .userUuid(userId)
                .totalPrice(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()).block();
        cartId = Objects.requireNonNull(cart).getUuid();

        ProductDao product = productRepository.save(ProductDao.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build()).block();
        productId = Objects.requireNonNull(product).getUuid();

        testProduct = Product.builder()
                .uuid(productId)
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .build();

        when(cartCacheService.evict(any())).thenReturn(Mono.empty());
        when(cartCacheService.getCart(userId)).thenReturn(Mono.just(
                Cart.builder()
                        .uuid(cartId)
                        .userUuid(userId)
                        .totalPrice(BigDecimal.ZERO)
                        .items(List.of())
                        .build()
        ));
    }

    @Test
    void addToCart_shouldAddProductToCart() {
        when(productService.getByUuid(productId)).thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.addToCart(userId, productId, 2))
                .assertNext(cart -> {
                    assertEquals(1, cart.getItems().size());
                    assertEquals(2, cart.getItems().getFirst().getQuantity());
                    assertEquals(0, cart.getTotalPrice().compareTo(BigDecimal.valueOf(200)));
                })
                .verifyComplete();

        CartDao persistedCart = cartRepository.findByUserUuid(userId).block();
        assertNotNull(persistedCart);
        assertEquals(0, persistedCart.getTotalPrice().compareTo(BigDecimal.valueOf(200)));

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cartId).collectList().block();
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(2, items.getFirst().getQuantity());
        assertEquals(productId, items.getFirst().getProductUuid());
    }

    @Test
    void addToCart_shouldUpdateQuantityForExistingProduct() {
        when(productService.getByUuid(productId)).thenReturn(Mono.just(testProduct));
        Cart cart1 = cartService.addToCart(userId, productId, 1).block();
        List<CartItemDao> items1 = cartItemRepository.findByCartUuid(Objects.requireNonNull(cart1).getUuid()).collectList().block();

        when(cartCacheService.getCart(userId)).thenReturn(Mono.just(
                Cart.builder()
                        .uuid(cartId)
                        .userUuid(userId)
                        .totalPrice(BigDecimal.ZERO)
                        .items(List.of(CartItem.builder()
                                .uuid(Objects.requireNonNull(items1).getFirst().getUuid())
                                .cartUuid(cart1.getUuid())
                                .quantity(items1.getFirst().getQuantity())
                                .product(Product.builder().uuid(items1.getFirst().getProductUuid()).build())
                                .build()))
                        .build()
        ));

        StepVerifier.create(cartService.addToCart(userId, productId, 2))
                .assertNext(cart -> {
                    assertEquals(1, cart.getItems().size());
                    assertEquals(3, cart.getItems().getFirst().getQuantity());
                    assertEquals(0, cart.getTotalPrice().compareTo(BigDecimal.valueOf(300)));
                })
                .verifyComplete();

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cartId).collectList().block();
        assertEquals(1, Objects.requireNonNull(items).size());
        assertEquals(3, items.getFirst().getQuantity());
    }

    @Test
    void addToCart_shouldRollbackOnError() {
        when(productService.getByUuid(productId))
                .thenReturn(Mono.error(new RuntimeException("Product service error")));

        StepVerifier.create(cartService.addToCart(userId, productId, 1))
                .expectErrorMatches(e -> e instanceof IllegalCartStateException &&
                        e.getMessage().contains("Не удалось добавить товар в корзину"))
                .verify();

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cartId).collectList().block();
        assertTrue(Objects.requireNonNull(items).isEmpty());
    }

    @Test
    void removeFromCart_shouldRemoveProduct() {
        when(productService.getByUuid(productId)).thenReturn(Mono.just(testProduct));
        cartService.addToCart(userId, productId, 1).block();

        StepVerifier.create(cartService.removeFromCart(userId, productId))
                .assertNext(cart -> {
                    assertTrue(cart.getItems().isEmpty());
                    assertEquals(0, cart.getTotalPrice().compareTo(BigDecimal.ZERO));
                })
                .verifyComplete();

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cartId).collectList().block();
        assertTrue(Objects.requireNonNull(items).isEmpty());
    }

    @Test
    void clear_shouldRemoveAllItems() {
        when(productService.getByUuid(productId)).thenReturn(Mono.just(testProduct));
        cartService.addToCart(userId, productId, 2).block();

        StepVerifier.create(cartService.clear(userId))
                .verifyComplete();

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cartId).collectList().block();
        assertTrue(Objects.requireNonNull(items).isEmpty());

        CartDao cart = cartRepository.findByUserUuid(userId).block();
        assertEquals(0, Objects.requireNonNull(cart).getTotalPrice().compareTo(BigDecimal.ZERO));
    }

    @Test
    void updateQuantity_shouldChangeProductQuantity() {
        when(productService.getByUuid(productId)).thenReturn(Mono.just(testProduct));
        Cart cart1 = cartService.addToCart(userId, productId, 1).block();
        List<CartItemDao> items1 = cartItemRepository.findByCartUuid(Objects.requireNonNull(cart1).getUuid()).collectList().block();

        when(cartCacheService.getCart(userId)).thenReturn(Mono.just(
                Cart.builder()
                        .uuid(cartId)
                        .userUuid(userId)
                        .totalPrice(BigDecimal.ZERO)
                        .items(List.of(CartItem.builder()
                                .uuid(Objects.requireNonNull(items1).getFirst().getUuid())
                                .cartUuid(cart1.getUuid())
                                .quantity(items1.getFirst().getQuantity())
                                .product(Product.builder().uuid(items1.getFirst().getProductUuid()).build())
                                .build()))
                        .build()
        ));

        StepVerifier.create(cartService.updateQuantity(userId, productId, 5))
                .assertNext(cart -> {
                    assertEquals(1, cart.getItems().size());
                    assertEquals(5, cart.getItems().getFirst().getQuantity());
                    assertEquals(0, cart.getTotalPrice().compareTo(BigDecimal.valueOf(500)));
                })
                .verifyComplete();

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cartId).collectList().block();
        assertEquals(5, Objects.requireNonNull(items).getFirst().getQuantity());
    }

    @Test
    void updateQuantity_shouldRejectInvalidQuantity() {
        StepVerifier.create(cartService.updateQuantity(userId, productId, 0))
                .expectError(IllegalCartStateException.class)
                .verify();
    }

    @Test
    void get_shouldReturnCartWithItems() {
        when(productService.getByUuid(productId)).thenReturn(Mono.just(testProduct));
        cartService.addToCart(userId, productId, 1).block();

        when(cartCacheService.getCart(userId)).thenReturn(Mono.just(
                Cart.builder()
                        .uuid(cartId)
                        .userUuid(userId)
                        .totalPrice(BigDecimal.valueOf(100))
                        .items(List.of(CartItem.builder().product(Product.builder().uuid(productId).build()).build()))
                        .build()
        ));

        StepVerifier.create(cartService.get(userId))
                .assertNext(cart -> {
                    assertEquals(cartId, cart.getUuid());
                    assertEquals(1, cart.getItems().size());
                    assertEquals(productId, cart.getItems().getFirst().getProduct().getUuid());
                })
                .verifyComplete();
    }
}