package ru.practicum.service.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import ru.practicum.exception.cart.CartNotFoundException;
import ru.practicum.exception.product.ProductNotFoundException;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;
import ru.practicum.model.product.Product;
import ru.practicum.repository.cart.CartRepository;
import ru.practicum.service.product.ProductService;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CacheManager cacheManager;

    @InjectMocks
    private CartServiceImpl cartService;

    private UUID testUserId;
    private UUID testProductId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testProductId = UUID.randomUUID();
    }

    @Test
    void create_shouldCreateNewCart() {
        Cart newCart = new Cart();
        newCart.setUserUuid(testUserId);

        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        Cart createdCart = cartService.create(testUserId);

        assertThat(createdCart.getUserUuid()).isEqualTo(testUserId);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void get_shouldReturnExistingCart() {
        Cart cart = new Cart();
        cart.setUserUuid(testUserId);

        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Optional.of(cart));

        Cart foundCart = cartService.get(testUserId);

        assertThat(foundCart.getUserUuid()).isEqualTo(testUserId);
    }

    @Test
    void get_shouldThrowExceptionWhenCartNotFound() {
        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.get(testUserId))
                .isInstanceOf(CartNotFoundException.class)
                .hasMessageContaining("не найдена");
    }

    @Test
    void addToCart_shouldAddNewProductToCart() {
        Cart cart = new Cart();
        cart.setUserUuid(testUserId);
        cart.setItems(new ArrayList<>());

        Product product = new Product();
        product.setUuid(testProductId);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));

        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Optional.of(cart));
        when(productService.getByUuid(testProductId)).thenReturn(product);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart updatedCart = cartService.addToCart(testUserId, testProductId, 2);

        assertThat(updatedCart.getItems()).hasSize(1);
        assertThat(updatedCart.getItems().getFirst().getProduct().getUuid()).isEqualTo(testProductId);
        assertThat(updatedCart.getItems().getFirst().getQuantity()).isEqualTo(2);
    }

    @Test
    void addToCart_shouldIncreaseQuantityForExistingProduct() {
        Cart cart = new Cart();
        cart.setUserUuid(testUserId);

        Product product = new Product();
        product.setUuid(testProductId);
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(100));

        CartItem existingItem = new CartItem();
        existingItem.setProduct(product);
        existingItem.setQuantity(1);
        existingItem.setCart(cart);

        cart.setItems(new ArrayList<>(List.of(existingItem)));

        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Optional.of(cart));
        when(productService.getByUuid(testProductId)).thenReturn(product);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart updatedCart = cartService.addToCart(testUserId, testProductId, 3);

        assertThat(updatedCart.getItems()).hasSize(1);
        assertThat(updatedCart.getItems().getFirst().getQuantity()).isEqualTo(4);
    }

    @Test
    void addToCart_shouldThrowExceptionWhenProductNotFound() {
        Cart cart = new Cart();
        cart.setUserUuid(testUserId);

        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Optional.of(cart));
        when(productService.getByUuid(testProductId)).thenThrow(new ProductNotFoundException("Product not found"));

        assertThatThrownBy(() -> cartService.addToCart(testUserId, testProductId, 1))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void removeFromCart_shouldRemoveProductFromCart() {
        Cart cart = new Cart();
        cart.setUserUuid(testUserId);

        Product product = new Product();
        product.setUuid(testProductId);
        product.setPrice(BigDecimal.TEN);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setCart(cart);

        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart updatedCart = cartService.removeFromCart(testUserId, testProductId);

        assertThat(updatedCart.getItems()).isEmpty();
    }

    @Test
    void clear_shouldRemoveAllItemsFromCart() {
        Cart cart = new Cart();
        cart.setUserUuid(testUserId);

        Product product = new Product();
        product.setUuid(testProductId);
        product.setPrice(BigDecimal.TEN);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setCart(cart);

        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.clear(testUserId);

        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    void updateQuantity_shouldChangeProductQuantity() {
        Cart cart = new Cart();
        cart.setUserUuid(testUserId);

        Product product = new Product();
        product.setUuid(testProductId);
        product.setPrice(BigDecimal.TEN);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setCart(cart);

        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.updateQuantity(testUserId, testProductId, 5);

        assertThat(cart.getItems().getFirst().getQuantity()).isEqualTo(5);
    }

    @Test
    void getCachedCart_shouldReturn() {
        Cart cart = new Cart();
        cart.setUserUuid(testUserId);

        Product product = new Product();
        product.setUuid(testProductId);
        product.setPrice(BigDecimal.valueOf(150));

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);
        item.setCart(cart);

        cart.setItems(new ArrayList<>(List.of(item)));

        when(cartRepository.findByUserUuid(testUserId)).thenReturn(Optional.of(cart));

        Cart cachedCart = cartService.getCachedCart(testUserId);

        assertThat(cachedCart).isNotNull();
    }
}