package ru.practicum.service.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.cart.CartDao;
import ru.practicum.exception.cart.IllegalCartStateException;
import ru.practicum.mapper.cart.CartItemMapper;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.product.Product;
import ru.practicum.repository.cart.CartItemRepository;
import ru.practicum.repository.cart.CartRepository;
import ru.practicum.service.product.ProductService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CartCacheService cartCacheService;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CartItemMapper cartItemMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private UUID userId;
    private UUID productId;
    private Cart cart;
    private CartDao cartDao;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        productId = UUID.randomUUID();

        cart = Cart.builder()
                .uuid(UUID.randomUUID())
                .userUuid(userId)
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();

        cartDao = CartDao.builder()
                .uuid(cart.getUuid())
                .userUuid(userId)
                .totalPrice(BigDecimal.ZERO)
                .updatedAt(LocalDateTime.now())
                .build();

        product = Product.builder()
                .uuid(productId)
                .name("Test Product")
                .price(BigDecimal.TEN)
                .build();

        cartItem = CartItem.builder()
                .cartUuid(cart.getUuid())
                .product(product)
                .quantity(1)
                .build();
    }

    @Test
    void createGuest_ShouldCreateNewCart() {
        when(cartMapper.cartToCartDao(any(Cart.class))).thenReturn(cartDao);
        when(cartRepository.save(any(CartDao.class))).thenReturn(Mono.just(cartDao));
        when(cartMapper.cartDaoToCart(any(CartDao.class))).thenReturn(cart);

        StepVerifier.create(cartService.createGuest(userId))
                .expectNextMatches(createdCart ->
                        createdCart.getUserUuid().equals(userId) &&
                                createdCart.getItems().isEmpty() &&
                                createdCart.getTotalPrice().equals(BigDecimal.ZERO)
                )
                .verifyComplete();

        verify(cartRepository).save(any(CartDao.class));
    }

    @Test
    void get_ShouldReturnCartWithItems() {
        when(cartCacheService.getCart(userId)).thenReturn(Mono.just(cart));
        when(cartMapper.cartToCartDao(cart)).thenReturn(cartDao);
        when(cartItemRepository.findByCartUuid(cart.getUuid())).thenReturn(Flux.empty());
        when(cartMapper.cartDaoToCart(cartDao)).thenReturn(cart);

        StepVerifier.create(cartService.get(userId))
                .expectNextMatches(retrievedCart ->
                        retrievedCart.getUserUuid().equals(userId) &&
                                retrievedCart.getItems().isEmpty()
                )
                .verifyComplete();

        verify(cartCacheService).getCart(userId);
        verify(cartItemRepository).findByCartUuid(cart.getUuid());
    }

    @Test
    void addToCart_WithInvalidQuantity_ShouldThrowException() {
        StepVerifier.create(cartService.addToCart(userId, productId, 0))
                .expectErrorMatches(ex ->
                        ex instanceof IllegalCartStateException &&
                                ex.getMessage().equals("Количество товара должно быть больше нуля")
                )
                .verify();
    }

    @Test
    void updateQuantity_WithInvalidQuantity_ShouldThrowException() {
        StepVerifier.create(cartService.updateQuantity(userId, productId, 0))
                .expectErrorMatches(ex ->
                        ex instanceof IllegalCartStateException &&
                                ex.getMessage().equals("Количество товара не может быть меньше или равно нулю")
                )
                .verify();
    }
}