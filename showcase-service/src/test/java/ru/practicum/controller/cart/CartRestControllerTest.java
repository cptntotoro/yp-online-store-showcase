package ru.practicum.controller.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.user.User;
import ru.practicum.service.cart.CartService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartRestControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartRestController cartRestController;

    private User testUser;
    private UUID testUserId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        productId = UUID.randomUUID();

        testUser = User.builder()
                .uuid(testUserId)
                .username("testuser")
                .password("password")
                .roles(List.of("USER"))
                .email("test@example.com")
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void addToCart_returnsTotalPrice() {
        Cart cart = new Cart();
        cart.setTotalPrice(BigDecimal.valueOf(200));

        when(cartService.addToCart(testUserId, productId, 3)).thenReturn(Mono.just(cart));

        BigDecimal result = cartRestController
                .addToCart(testUser, productId, 3)
                .block();

        assertEquals(BigDecimal.valueOf(200), result);
        verify(cartService).addToCart(testUserId, productId, 3);
    }

    @Test
    void updateCartItem_returnsUpdatedTotalPrice() {
        Cart updatedCart = new Cart();
        updatedCart.setTotalPrice(BigDecimal.valueOf(350));

        when(cartService.updateQuantity(testUserId, productId, 5)).thenReturn(Mono.empty());
        when(cartService.get(testUserId)).thenReturn(Mono.just(updatedCart));

        BigDecimal result = cartRestController
                .updateCartItem(testUser, productId, 5)
                .block();

        assertEquals(BigDecimal.valueOf(350), result);
        verify(cartService).updateQuantity(testUserId, productId, 5);
        verify(cartService).get(testUserId);
    }

    @Test
    void removeFromCart_returnsUpdatedTotalPrice() {
        Cart cart = new Cart();
        cart.setTotalPrice(BigDecimal.ZERO);

        when(cartService.removeFromCart(testUserId, productId)).thenReturn(Mono.just(cart));

        BigDecimal result = cartRestController
                .removeFromCart(testUser, productId)
                .block();

        assertEquals(BigDecimal.ZERO, result);
        verify(cartService).removeFromCart(testUserId, productId);
    }
}
