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
import ru.practicum.dao.cart.CartItemDao;
import ru.practicum.exception.cart.CartNotFoundException;
import ru.practicum.exception.product.ProductNotFoundException;
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
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartServiceImpl cartService;

    private UUID testUserId;
    private UUID testProductId;
    private CartDao cartDao;
    private Cart cart;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testProductId = UUID.randomUUID();

        cartDao = CartDao.builder()
                .uuid(UUID.randomUUID())
                .userUuid(testUserId)
                .totalPrice(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        cart = Cart.builder()
                .uuid(cartDao.getUuid())
                .userUuid(testUserId)
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .createdAt(cartDao.getCreatedAt())
                .updatedAt(cartDao.getUpdatedAt())
                .build();
    }

    @Test
    void create_shouldCreateGuestNewCart() {
        when(cartMapper.cartToCartDao(any())).thenReturn(cartDao);
        when(cartRepository.save(any())).thenReturn(Mono.just(cartDao));
        when(cartMapper.cartDaoToCart(any())).thenReturn(cart);

        StepVerifier.create(cartService.createGuest(testUserId))
                .expectNextMatches(c -> c.getUserUuid().equals(testUserId))
                .verifyComplete();

        verify(cartRepository).save(any());
    }

    @Test
    void get_shouldReturnExistingCart() {
        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.findByCartUuid(cartDao.getUuid())).thenReturn(Flux.empty());
        when(cartMapper.cartDaoToCart(cartDao)).thenReturn(cart);

        StepVerifier.create(cartService.get(testUserId))
                .expectNextMatches(c -> c.getUserUuid().equals(testUserId))
                .verifyComplete();
    }

    @Test
    void get_shouldThrowExceptionWhenCartNotFound() {
        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.get(testUserId))
                .expectError(CartNotFoundException.class)
                .verify();
    }

    @Test
    void addToCart_shouldAddNewProductToCart() {
        Product product = Product.builder()
                .uuid(testProductId)
                .price(BigDecimal.TEN)
                .build();

        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.findByCartUuid(cartDao.getUuid())).thenReturn(Flux.empty());
        when(productService.getByUuid(testProductId)).thenReturn(Mono.just(product));
        when(cartMapper.cartDaoToCart(cartDao)).thenReturn(cart);
        when(cartItemMapper.cartItemToCartItemDao(any())).thenReturn(new CartItemDao());
        when(cartItemRepository.save(any())).thenReturn(Mono.just(new CartItemDao()));
        when(cartRepository.save(any())).thenReturn(Mono.just(cartDao));
        when(cartMapper.cartToCartDao(any())).thenReturn(cartDao);

        StepVerifier.create(cartService.addToCart(testUserId, testProductId, 2))
                .expectNextMatches(c -> c.getUserUuid().equals(testUserId))
                .verifyComplete();
    }

    @Test
    void addToCart_shouldIncreaseQuantityForExistingProduct() {
        Product product = Product.builder()
                .uuid(testProductId)
                .price(BigDecimal.valueOf(100))
                .build();

        CartItem existingItem = CartItem.builder()
                .product(product)
                .quantity(1)
                .build();

        cart.setItems(List.of(existingItem));

        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.findByCartUuid(cartDao.getUuid())).thenReturn(Flux.empty());
        when(productService.getByUuid(testProductId)).thenReturn(Mono.just(product));
        when(cartMapper.cartDaoToCart(cartDao)).thenReturn(cart);
        when(cartItemMapper.cartItemToCartItemDao(any())).thenReturn(new CartItemDao());
        when(cartItemRepository.save(any())).thenReturn(Mono.just(new CartItemDao()));
        when(cartRepository.save(any())).thenReturn(Mono.just(cartDao));
        when(cartMapper.cartToCartDao(any())).thenReturn(cartDao);

        StepVerifier.create(cartService.addToCart(testUserId, testProductId, 3))
                .assertNext(result -> {
                    assertThat(result.getItems()).hasSize(1);
                    assertThat(result.getItems().getFirst().getQuantity()).isEqualTo(4);
                })
                .verifyComplete();
    }

    @Test
    void addToCart_shouldThrowExceptionWhenProductNotFound() {
        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.findByCartUuid(cartDao.getUuid())).thenReturn(Flux.empty());
        when(cartMapper.cartDaoToCart(cartDao)).thenReturn(cart);
        when(productService.getByUuid(testProductId)).thenReturn(Mono.error(new ProductNotFoundException("Product not found")));

        StepVerifier.create(cartService.addToCart(testUserId, testProductId, 1))
                .expectErrorMatches(e ->
                        e instanceof ProductNotFoundException &&
                                e.getMessage().contains("Product not found"))
                .verify();
    }

    @Test
    void removeFromCart_shouldRemoveProductFromCart() {
        Product product = Product.builder().uuid(testProductId).build();

        CartItemDao item = CartItemDao.builder()
                .uuid(UUID.randomUUID())
                .cartUuid(cartDao.getUuid())
                .productUuid(product.getUuid())
                .quantity(1)
                .createdAt(LocalDateTime.now())
                .build();

        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.findByCartUuid(cartDao.getUuid())).thenReturn(Flux.just(item));
        when(cartItemRepository.deleteByCartUuidAndProductUuid(any(), any())).thenReturn(Mono.empty());
        when(cartRepository.save(any())).thenReturn(Mono.just(cartDao));
        when(cartMapper.cartDaoToCart(cartDao)).thenReturn(cart);
        when(cartMapper.cartToCartDao(any())).thenReturn(cartDao);

        StepVerifier.create(cartService.removeFromCart(testUserId, testProductId))
                .expectNextMatches(c -> c.getUserUuid().equals(testUserId))
                .verifyComplete();
    }

    @Test
    void clear_shouldRemoveAllItemsFromCart() {
        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.deleteByCartUuid(cartDao.getUuid())).thenReturn(Mono.empty());
        when(cartRepository.save(any())).thenReturn(Mono.just(cartDao));

        StepVerifier.create(cartService.clear(testUserId))
                .verifyComplete();
    }

    @Test
    void updateQuantity_shouldChangeProductQuantity() {
        Product product = Product.builder().uuid(testProductId).price(BigDecimal.TEN).build();

        CartItemDao item = CartItemDao.builder()
                .uuid(UUID.randomUUID())
                .cartUuid(cartDao.getUuid())
                .productUuid(product.getUuid())
                .quantity(1)
                .createdAt(LocalDateTime.now())
                .build();

        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.findByCartUuid(cartDao.getUuid())).thenReturn(Flux.just(item));
        when(cartItemRepository.save(any())).thenReturn(Mono.just(item));
        when(cartRepository.save(any())).thenReturn(Mono.just(cartDao));
        when(cartMapper.cartDaoToCart(cartDao)).thenReturn(cart);
        when(cartMapper.cartToCartDao(any())).thenReturn(cartDao);

        StepVerifier.create(cartService.updateQuantity(testUserId, testProductId, 5))
                .expectNextMatches(c -> c.getUserUuid().equals(testUserId))
                .verifyComplete();
    }
}