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
import java.util.List;
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
    private UUID cartId;
    private UUID productId;
    private Cart cart;
    private CartDao cartDao;
    private Product product;
    private CartItem cartItem;
    private CartItemDao cartItemDao;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        cartId = UUID.randomUUID();
        productId = UUID.randomUUID();

        product = Product.builder()
                .uuid(productId)
                .price(BigDecimal.valueOf(100))
                .build();

        cartItem = new CartItem(UUID.randomUUID(), cartId, product, 2, LocalDateTime.now());
        cartItemDao = new CartItemDao(cartItem.getUuid(), cartId, productId, 2, LocalDateTime.now());

        cart = Cart.builder()
                .uuid(cartId)
                .userUuid(userId)
                .items(List.of(cartItem))
                .totalPrice(BigDecimal.valueOf(200))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        cartDao = new CartDao(
                cartId,
                userId,
                BigDecimal.valueOf(200),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void createGuest_ShouldCreateNewCart() {
        Cart newCart = Cart.builder()
                .uuid(UUID.randomUUID())
                .userUuid(userId)
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CartDao newCartDao = new CartDao(
                newCart.getUuid(),
                userId,
                BigDecimal.ZERO,
                newCart.getCreatedAt(),
                newCart.getUpdatedAt()
        );

        when(cartMapper.cartToCartDao(any(Cart.class))).thenReturn(cartDao);
        when(cartRepository.save(any(CartDao.class))).thenReturn(Mono.just(newCartDao));

        StepVerifier.create(cartService.createGuest(userId))
                .expectNextMatches(createdCart ->
                        createdCart.getUserUuid().equals(userId) &&
                                createdCart.getItems().isEmpty() &&
                                createdCart.getTotalPrice().equals(BigDecimal.ZERO))
                .verifyComplete();

        verify(cartRepository).save(any(CartDao.class));
    }

    @Test
    void get_ShouldReturnCartWithItems() {
        when(cartRepository.findByUserUuid(userId)).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.findByCartUuid(cartId)).thenReturn(Flux.just(cartItemDao));
        when(productService.getByUuid(productId)).thenReturn(Mono.just(product));
        when(cartMapper.cartDaoToCart(any(CartDao.class))).thenReturn(cart);
        when(cartItemMapper.cartItemDaoToCartItem(any(CartItemDao.class))).thenReturn(cartItem);

        StepVerifier.create(cartService.get(userId))
                .expectNextMatches(cart ->
                        cart.getUserUuid().equals(userId) &&
                                cart.getItems().size() == 1 &&
                                cart.getTotalPrice().equals(BigDecimal.valueOf(200)))
                .verifyComplete();
    }

    @Test
    void get_ShouldReturnEmptyCart_WhenNoItems() {
        Cart emptyCart = Cart.builder()
                .uuid(cartId)
                .userUuid(userId)
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CartDao emptyCartDao = new CartDao(
                cartId,
                userId,
                BigDecimal.ZERO,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(cartRepository.findByUserUuid(userId)).thenReturn(Mono.just(emptyCartDao));
        when(cartItemRepository.findByCartUuid(cartId)).thenReturn(Flux.empty());
        when(cartMapper.cartDaoToCart(emptyCartDao)).thenReturn(emptyCart);

        StepVerifier.create(cartService.get(userId))
                .expectNextMatches(cart ->
                        cart.getItems().isEmpty() &&
                                cart.getTotalPrice().compareTo(BigDecimal.ZERO) == 0
                )
                .verifyComplete();
    }

    @Test
    void addToCart_ShouldAddNewItem() {
        UUID newProductId = UUID.randomUUID();
        Product newProduct = Product.builder()
                .uuid(newProductId)
                .price(BigDecimal.valueOf(50))
                .build();

        when(cartMapper.cartToCartDao(any(Cart.class))).thenReturn(cartDao);
        when(cartMapper.cartDaoToCart(any(CartDao.class))).thenReturn(cart);
        when(cartItemMapper.cartItemToCartItemDao(any(CartItem.class))).thenReturn(cartItemDao);
        when(cartItemMapper.cartItemDaoToCartItem(any(CartItemDao.class))).thenReturn(cartItem);
        when(cartRepository.findByUserUuid(userId)).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.findByCartUuid(cartId)).thenReturn(Flux.just(cartItemDao));
        when(productService.getByUuid(productId)).thenReturn(Mono.just(product));
        when(productService.getByUuid(newProductId)).thenReturn(Mono.just(newProduct));
        when(cartRepository.save(any(CartDao.class))).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.save(any(CartItemDao.class))).thenReturn(Mono.just(new CartItemDao()));

        StepVerifier.create(cartService.addToCart(userId, newProductId, 3))
                .expectNextMatches(cart ->
                        cart.getItems().size() == 2 &&
                                cart.getTotalPrice().equals(BigDecimal.valueOf(350)))
                .verifyComplete();

        verify(cartCacheService).evict(userId);
    }

    @Test
    void addToCart_ShouldUpdateExistingItem() {
        when(cartMapper.cartToCartDao(any(Cart.class))).thenReturn(cartDao);
        when(cartMapper.cartDaoToCart(any(CartDao.class))).thenReturn(cart);
        when(cartItemMapper.cartItemToCartItemDao(any(CartItem.class))).thenReturn(cartItemDao);
        when(cartItemMapper.cartItemDaoToCartItem(any(CartItemDao.class))).thenReturn(cartItem);
        when(cartRepository.findByUserUuid(userId)).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.findByCartUuid(cartId)).thenReturn(Flux.just(cartItemDao));
        when(productService.getByUuid(productId)).thenReturn(Mono.just(product));
        when(cartRepository.save(any(CartDao.class))).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.save(any(CartItemDao.class))).thenReturn(Mono.just(cartItemDao));

        StepVerifier.create(cartService.addToCart(userId, productId, 1))
                .expectNextMatches(cart ->
                        cart.getItems().get(0).getQuantity() == 3 &&
                                cart.getTotalPrice().equals(BigDecimal.valueOf(300)))
                .verifyComplete();
    }

    @Test
    void addToCart_ShouldFail_WhenQuantityInvalid() {
        StepVerifier.create(cartService.addToCart(userId, productId, 0))
                .expectError(IllegalCartStateException.class)
                .verify();

        verifyNoInteractions(cartRepository, productService);
    }

    @Test
    void removeFromCart_ShouldRemoveItem() {
        when(cartMapper.cartToCartDao(any(Cart.class))).thenReturn(cartDao);
        when(cartMapper.cartDaoToCart(any(CartDao.class))).thenReturn(cart);
        when(cartItemMapper.cartItemDaoToCartItem(any(CartItemDao.class))).thenReturn(cartItem);
        when(cartRepository.findByUserUuid(userId)).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.findByCartUuid(cartId)).thenReturn(Flux.just(cartItemDao));
        when(productService.getByUuid(productId)).thenReturn(Mono.just(product));
        when(cartRepository.save(any(CartDao.class))).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.deleteByCartUuidAndProductUuid(cartId, productId)).thenReturn(Mono.empty());

        StepVerifier.create(cartService.removeFromCart(userId, productId))
                .expectNextMatches(cart ->
                        cart.getItems().isEmpty() &&
                                cart.getTotalPrice().equals(BigDecimal.ZERO))
                .verifyComplete();

        verify(cartCacheService).evict(userId);
    }

    @Test
    void clear_ShouldRemoveAllItems() {
        when(cartRepository.findByUserUuid(userId)).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.findByCartUuid(cartId)).thenReturn(Flux.just(cartItemDao));
        when(productService.getByUuid(productId)).thenReturn(Mono.just(product));
        when(cartItemRepository.deleteByCartUuid(cartId)).thenReturn(Mono.empty());
        when(cartRepository.save(any(CartDao.class))).thenReturn(Mono.just(cartDao));

        when(cartMapper.cartDaoToCart(cartDao)).thenReturn(cart);
        when(cartItemMapper.cartItemDaoToCartItem(cartItemDao)).thenReturn(cartItem);

        StepVerifier.create(cartService.clear(userId))
                .verifyComplete();

        verify(cartCacheService).evict(userId);
        verify(cartItemRepository).deleteByCartUuid(cartId);
        verify(cartRepository).save(any(CartDao.class));
    }

    @Test
    void updateQuantity_ShouldUpdateItemQuantity() {
        when(cartMapper.cartToCartDao(any(Cart.class))).thenReturn(cartDao);
        when(cartMapper.cartDaoToCart(any(CartDao.class))).thenReturn(cart);
        when(cartItemMapper.cartItemToCartItemDao(any(CartItem.class))).thenReturn(cartItemDao);
        when(cartItemMapper.cartItemDaoToCartItem(any(CartItemDao.class))).thenReturn(cartItem);
        when(cartRepository.findByUserUuid(userId)).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.findByCartUuid(cartId)).thenReturn(Flux.just(cartItemDao));
        when(productService.getByUuid(productId)).thenReturn(Mono.just(product));
        when(cartRepository.save(any(CartDao.class))).thenReturn(Mono.just(cartDao));
        when(cartItemRepository.save(any(CartItemDao.class))).thenReturn(Mono.just(cartItemDao));

        StepVerifier.create(cartService.updateQuantity(userId, productId, 5))
                .expectNextMatches(cart ->
                        cart.getItems().get(0).getQuantity() == 5 &&
                                cart.getTotalPrice().equals(BigDecimal.valueOf(500)))
                .verifyComplete();
    }

    @Test
    void updateQuantity_ShouldFail_WhenQuantityInvalid() {
        StepVerifier.create(cartService.updateQuantity(userId, productId, 0))
                .expectError(IllegalCartStateException.class)
                .verify();

        verifyNoInteractions(cartRepository, productService);
    }
}