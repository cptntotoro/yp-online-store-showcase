package ru.practicum.controller.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseControllerTest;
import ru.practicum.dto.cart.CartDto;
import ru.practicum.model.cart.Cart;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartViewControllerTest extends BaseControllerTest {

    private final UUID TEST_PRODUCT_UUID = UUID.randomUUID();
    private final BigDecimal TEST_TOTAL_PRICE = new BigDecimal("99.99");


    @BeforeEach
    void setUp() {
        super.baseSetUp();
    }

    @Test
    void showCart_ShouldReturnCartView_WhenCartExists() {
        Cart cart = new Cart();
        CartDto cartDto = new CartDto();
        cartDto.setTotalPrice(TEST_TOTAL_PRICE);

        when(cartService.get(TEST_USER_UUID)).thenReturn(Mono.just(cart));
        when(cartMapper.cartToCartDto(cart)).thenReturn(cartDto);

        getWebTestClientWithMockUser()
                .get()
                .uri("/cart")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void removeFromCart_ShouldRedirect() {
        when(cartService.removeFromCart(TEST_USER_UUID, TEST_PRODUCT_UUID))
                .thenReturn(Mono.empty());

        getWebTestClientWithMockUser().post()
                .uri("/cart/remove/{productUuid}", TEST_PRODUCT_UUID)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart");

        verify(cartService).removeFromCart(TEST_USER_UUID, TEST_PRODUCT_UUID);
    }

    @Test
    void clearCart_ShouldRedirect() {
        when(cartService.clear(TEST_USER_UUID))
                .thenReturn(Mono.empty());

        getWebTestClientWithMockUser().post()
                .uri("/cart/clear")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/cart");

        verify(cartService).clear(TEST_USER_UUID);
    }
}