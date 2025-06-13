package ru.practicum.controller.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.practicum.config.WebAttributes;
import ru.practicum.model.cart.Cart;
import ru.practicum.service.cart.CartService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartRestControllerTest {

    private WebTestClient webTestClient;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartRestController cartRestController;

    private final UUID testUserUuid = UUID.randomUUID();
    private final UUID testProductUuid = UUID.randomUUID();
    private final BigDecimal testTotalPrice = new BigDecimal("99.99");

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(cartRestController)
                .webFilter((exchange, chain) -> {
                    exchange.getAttributes().put(WebAttributes.USER_UUID, testUserUuid);
                    return chain.filter(exchange);
                })
                .build();
    }

    @Test
    void addToCart_ShouldReturnTotalPrice() {
        Cart mockCart = new Cart();
        mockCart.setTotalPrice(testTotalPrice);

        when(cartService.addToCart(testUserUuid, testProductUuid, 1))
                .thenReturn(Mono.just(mockCart));

        webTestClient.post()
                .uri("/cart/add/{productUuid}?quantity=1", testProductUuid)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BigDecimal.class)
                .isEqualTo(testTotalPrice);

        verify(cartService).addToCart(testUserUuid, testProductUuid, 1);
    }

    @Test
    void updateCartItem_ShouldReturnUpdatedTotalPrice() {
        Cart mockCart = new Cart();
        mockCart.setTotalPrice(testTotalPrice);

        when(cartService.updateQuantity(testUserUuid, testProductUuid, 2))
                .thenReturn(Mono.empty());
        when(cartService.get(testUserUuid))
                .thenReturn(Mono.just(mockCart));

        webTestClient.patch()
                .uri("/cart/update/{productUuid}?quantity=2", testProductUuid)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BigDecimal.class)
                .isEqualTo(testTotalPrice);

        verify(cartService).updateQuantity(testUserUuid, testProductUuid, 2);
        verify(cartService).get(testUserUuid);
    }

    @Test
    void removeFromCart_ShouldReturnUpdatedTotalPrice() {
        var mockCart = new Cart();
        mockCart.setTotalPrice(testTotalPrice);

        when(cartService.removeFromCart(testUserUuid, testProductUuid))
                .thenReturn(Mono.just(mockCart));

        webTestClient.delete()
                .uri("/cart/remove/{productUuid}", testProductUuid)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BigDecimal.class)
                .isEqualTo(testTotalPrice);

        verify(cartService).removeFromCart(testUserUuid, testProductUuid);
    }
}