package ru.practicum.service.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.cart.CartDao;
import ru.practicum.dao.cart.CartItemDao;
import ru.practicum.dao.product.ProductDao;
import ru.practicum.dao.user.UserDao;
import ru.practicum.exception.cart.IllegalCartStateException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CartServiceTransactionalTest {

    @Autowired
    private CartService cartService;

    @MockitoSpyBean
    private CartRepository cartRepository;

    @MockitoSpyBean
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @MockBean
    private ProductService productService;

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
    }

    @Test
    void addToCart_shouldRollbackWhenProductServiceFails() {
        when(productService.getByUuid(productId))
                .thenReturn(Mono.error(new RuntimeException("Product service error")));

        StepVerifier.create(cartService.addToCart(userId, productId, 2))
                .expectErrorMatches(e -> e.getMessage().contains("Не удалось добавить товар в корзину"))
                .verify();

        CartDao cart = cartRepository.findByUserUuid(userId).block();
        assertNotNull(cart);
        assertTrue(Objects.requireNonNull(cartItemRepository.findByCartUuid(cart.getUuid()).collectList().block()).isEmpty());
        assertEquals(0, cart.getTotalPrice().compareTo(BigDecimal.ZERO));
    }

    @Test
    void addToCart_shouldRollbackWhenCartItemSaveFails() {
        when(productService.getByUuid(productId))
                .thenReturn(Mono.just(testProduct));

        Mockito.doAnswer(invocation -> {
            CartItemDao itemDao = invocation.getArgument(0);
            if (itemDao.getProductUuid().equals(productId)) {
                return Mono.error(new RuntimeException("CartItem save error"));
            }
            return Mono.just(itemDao);
        }).when(cartItemRepository).save(any(CartItemDao.class));

        StepVerifier.create(cartService.addToCart(userId, productId, 2))
                .expectErrorMatches(e -> e.getMessage().contains("Не удалось добавить товар в корзину"))
                .verify();

        CartDao cart = cartRepository.findByUserUuid(userId).block();
        assertNotNull(cart);
        assertTrue(Objects.requireNonNull(cartItemRepository.findByCartUuid(cart.getUuid()).collectList().block()).isEmpty());
        assertEquals(0, cart.getTotalPrice().compareTo(BigDecimal.ZERO));
    }

    @Test
    void addToCart_shouldCommitWhenSuccessful() {
        when(productService.getByUuid(productId))
                .thenReturn(Mono.just(testProduct));

        StepVerifier.create(cartService.addToCart(userId, productId, 2))
                .assertNext(cart -> {
                    assertNotNull(cart);
                    assertEquals(1, cart.getItems().size());
                    assertEquals(2, cart.getItems().getFirst().getQuantity());
                    assertEquals(0, cart.getTotalPrice().compareTo(BigDecimal.valueOf(200)));
                    assertEquals(productId, cart.getItems().getFirst().getProduct().getUuid());
                })
                .verifyComplete();

        CartDao persistedCart = cartRepository.findByUserUuid(userId).block();
        assertNotNull(persistedCart);
        assertEquals(0, persistedCart.getTotalPrice().compareTo(BigDecimal.valueOf(200)));

        List<CartItemDao> persistedItems = cartItemRepository.findByCartUuid(persistedCart.getUuid())
                .collectList()
                .block();
        assertNotNull(persistedItems);
        assertEquals(1, persistedItems.size());
        assertEquals(2, persistedItems.getFirst().getQuantity());
        assertEquals(productId, persistedItems.getFirst().getProductUuid());
    }

    @Test
    void addToCart_shouldNotProceedOnNegativeQuantity() {
        StepVerifier.create(cartService.addToCart(userId, productId, -1))
                .expectError(IllegalCartStateException.class)
                .verify();

        verify(productService, never()).getByUuid(any());
    }

    @Test
    void removeFromCart_shouldRollbackWhenCartItemDeleteFails() {
        when(productService.getByUuid(productId))
                .thenReturn(Mono.just(testProduct));

        cartService.addToCart(userId, productId, 2).block();

        when(cartItemRepository.deleteByCartUuidAndProductUuid(cartId, productId))
                .thenReturn(Mono.error(new RuntimeException("Delete error")));

        StepVerifier.create(cartService.removeFromCart(userId, productId))
                .expectErrorMatches(e -> e.getMessage().contains("Не удалось удалить товар из корзины"))
                .verify();

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cartId)
                .collectList()
                .block();
        assertEquals(1, Objects.requireNonNull(items).size());
        assertEquals(productId, items.getFirst().getProductUuid());
    }

    @Test
    void removeFromCart_shouldRollbackWhenCartUpdateFails() {
        when(productService.getByUuid(productId))
                .thenReturn(Mono.just(testProduct));

        cartService.addToCart(userId, productId, 2).block();

        CartDao existingCart = cartRepository.findByUserUuid(userId).block();
        assertNotNull(existingCart);

        Mockito.doAnswer(invocation -> {
            CartDao cartDao = invocation.getArgument(0);
            if (cartDao.getUuid().equals(existingCart.getUuid()) &&
                    cartDao.getTotalPrice().equals(BigDecimal.ZERO)) {
                return Mono.error(new RuntimeException("Save error"));
            }
            return Mono.just(cartDao);
        }).when(cartRepository).save(any(CartDao.class));

        StepVerifier.create(cartService.removeFromCart(userId, productId))
                .expectErrorMatches(e -> e.getMessage().contains("Не удалось удалить товар из корзины"))
                .verify();

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cartId)
                .collectList()
                .block();
        assertEquals(1, Objects.requireNonNull(items).size());
        assertEquals(productId, items.getFirst().getProductUuid());
    }

    @Test
    void removeFromCart_shouldCommitWhenSuccessful() {
        when(productService.getByUuid(productId))
                .thenReturn(Mono.just(testProduct));

        cartService.addToCart(userId, productId, 2).block();

        StepVerifier.create(cartService.removeFromCart(userId, productId))
                .assertNext(cart -> {
                    assertNotNull(cart);
                    assertTrue(cart.getItems().isEmpty());
                    assertEquals(0, cart.getTotalPrice().compareTo(BigDecimal.ZERO));
                })
                .verifyComplete();

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cartId)
                .collectList()
                .block();
        assertTrue(Objects.requireNonNull(items).isEmpty());

        CartDao cart = cartRepository.findById(cartId).block();
        assertNotNull(cart);
        assertEquals(0, cart.getTotalPrice().compareTo(BigDecimal.ZERO));
    }

    @Test
    void clear_shouldRollbackWhenCartItemDeleteFails() {
        when(productService.getByUuid(productId))
                .thenReturn(Mono.just(testProduct));

        cartService.addToCart(userId, productId, 2).block();

        when(cartItemRepository.deleteByCartUuid(cartId))
                .thenReturn(Mono.error(new RuntimeException("Delete error")));

        StepVerifier.create(cartService.clear(userId))
                .expectErrorMatches(e -> e.getMessage().contains("Не удалось очистить корзину"))
                .verify();

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cartId)
                .collectList()
                .block();
        assertEquals(1, Objects.requireNonNull(items).size());
    }

    @Test
    void clear_shouldRollbackWhenCartUpdateFails() {
        when(productService.getByUuid(productId))
                .thenReturn(Mono.just(testProduct));

        cartService.addToCart(userId, productId, 2).block();

        CartDao existingCart = cartRepository.findByUserUuid(userId).block();
        assertNotNull(existingCart);

        Mockito.doAnswer(invocation -> {
            CartDao cartDao = invocation.getArgument(0);
            if (cartDao.getUuid().equals(existingCart.getUuid()) &&
                    cartDao.getTotalPrice().equals(BigDecimal.ZERO)) {
                return Mono.error(new RuntimeException("Save error"));
            }
            return Mono.just(cartDao);
        }).when(cartRepository).save(any(CartDao.class));

        StepVerifier.create(cartService.clear(userId))
                .expectErrorMatches(e -> e.getMessage().contains("Не удалось очистить корзину"))
                .verify();

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cartId)
                .collectList()
                .block();
        assertEquals(1, Objects.requireNonNull(items).size());
    }

    @Test
    void clear_shouldCommitWhenSuccessful() {
        when(productService.getByUuid(productId))
                .thenReturn(Mono.just(testProduct));

        cartService.addToCart(userId, productId, 2).block();

        StepVerifier.create(cartService.clear(userId))
                .verifyComplete();

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cartId)
                .collectList()
                .block();
        assertTrue(Objects.requireNonNull(items).isEmpty());

        CartDao cart = cartRepository.findById(cartId).block();
        assertNotNull(cart);
        assertEquals(0, cart.getTotalPrice().compareTo(BigDecimal.ZERO));
    }

    @Test
    void updateQuantity_shouldFailForZeroOrNegativeQuantity() {
        StepVerifier.create(cartService.updateQuantity(userId, productId, 0))
                .expectErrorMatches(e -> e instanceof IllegalCartStateException &&
                        e.getMessage().contains("Количество товара не может быть меньше или равно нулю"))
                .verify();

        StepVerifier.create(cartService.updateQuantity(userId, productId, -1))
                .expectErrorMatches(e -> e instanceof IllegalCartStateException &&
                        e.getMessage().contains("Количество товара не может быть меньше или равно нулю"))
                .verify();
    }

    @Test
    void updateQuantity_shouldUpdateQuantityWhenProductInCart() {
        when(productService.getByUuid(productId))
                .thenReturn(Mono.just(testProduct));

        cartService.addToCart(userId, productId, 1).block();

        StepVerifier.create(cartService.updateQuantity(userId, productId, 3))
                .assertNext(cart -> {
                    assertEquals(1, cart.getItems().size());
                    assertEquals(3, cart.getItems().getFirst().getQuantity());
                    assertEquals(0, cart.getTotalPrice().compareTo(BigDecimal.valueOf(300)));
                })
                .verifyComplete();

        CartDao persistedCart = cartRepository.findByUserUuid(userId).block();
        assertNotNull(persistedCart);
        assertEquals(0, persistedCart.getTotalPrice().compareTo(BigDecimal.valueOf(300)));

        List<CartItemDao> persistedItems = cartItemRepository.findByCartUuid(persistedCart.getUuid())
                .collectList()
                .block();
        assertNotNull(persistedItems);
        assertEquals(1, persistedItems.size());
        assertEquals(3, persistedItems.getFirst().getQuantity());
    }

    @Test
    void updateQuantity_shouldRollbackWhenCartSaveFails() {
        when(productService.getByUuid(productId))
                .thenReturn(Mono.just(testProduct));

        cartService.addToCart(userId, productId, 1).block();

        CartDao existingCart = cartRepository.findByUserUuid(userId).block();
        assertNotNull(existingCart);

        Mockito.doAnswer(invocation -> {
            CartDao cartDao = invocation.getArgument(0);
            if (cartDao.getUuid().equals(existingCart.getUuid()) &&
                    cartDao.getTotalPrice().equals(BigDecimal.valueOf(300))) {
                return Mono.error(new RuntimeException("Save error"));
            }
            return Mono.just(cartDao);
        }).when(cartRepository).save(any(CartDao.class));

        StepVerifier.create(cartService.updateQuantity(userId, productId, 3))
                .expectErrorMatches(e -> e instanceof IllegalCartStateException &&
                        e.getMessage().contains("Не удалось обновить товар в корзине"))
                .verify();

        CartDao cart = cartRepository.findByUserUuid(userId).block();
        assertNotNull(cart);
        assertEquals(0, cart.getTotalPrice().compareTo(BigDecimal.valueOf(100)));

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cart.getUuid())
                .collectList()
                .block();
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(1, items.getFirst().getQuantity());
    }

    @Test
    void updateQuantity_shouldRollbackWhenCartItemsSaveFails() {
        when(productService.getByUuid(productId))
                .thenReturn(Mono.just(testProduct));

        cartService.addToCart(userId, productId, 1).block();

        Mockito.doAnswer(invocation -> {
            CartItemDao itemDao = invocation.getArgument(0);
            if (itemDao.getQuantity() == 3) {
                return Mono.error(new RuntimeException("CartItem save error"));
            }
            return Mono.just(itemDao);
        }).when(cartItemRepository).save(any(CartItemDao.class));

        StepVerifier.create(cartService.updateQuantity(userId, productId, 3))
                .expectErrorMatches(e -> e instanceof IllegalCartStateException &&
                        e.getMessage().contains("Не удалось обновить товар в корзине"))
                .verify();

        CartDao cart = cartRepository.findByUserUuid(userId).block();
        assertNotNull(cart);
        assertEquals(0, cart.getTotalPrice().compareTo(BigDecimal.valueOf(100)));

        List<CartItemDao> items = cartItemRepository.findByCartUuid(cart.getUuid())
                .collectList()
                .block();
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(1, items.getFirst().getQuantity());
    }
}