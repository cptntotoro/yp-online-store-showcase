package ru.practicum.controller.cart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
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

    private final UUID TEST_USER_UUID = UUID.randomUUID();
    private final UUID TEST_PRODUCT_UUID = UUID.randomUUID();
    private final BigDecimal TEST_TOTAL_PRICE = new BigDecimal("99.99");

//    @BeforeEach
//    void setUp() {
//        webTestClient = WebTestClient.bindToController(cartRestController)
//                .webFilter((exchange, chain) -> {
//                    exchange.getAttributes().put(WebAttributes.USER_UUID, TEST_USER_UUID);
//                    return chain.filter(exchange);
//                })
//                .build();
//    }

    @Test
    void addToCart_ShouldReturnTotalPrice() {
        Cart mockCart = new Cart();
        mockCart.setTotalPrice(TEST_TOTAL_PRICE);

        when(cartService.addToCart(TEST_USER_UUID, TEST_PRODUCT_UUID, 1))
                .thenReturn(Mono.just(mockCart));

        webTestClient.post()
                .uri("/cart/add/{productUuid}?quantity=1", TEST_PRODUCT_UUID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BigDecimal.class)
                .isEqualTo(TEST_TOTAL_PRICE);

        verify(cartService).addToCart(TEST_USER_UUID, TEST_PRODUCT_UUID, 1);
    }

    @Test
    void updateCartItem_ShouldReturnUpdatedTotalPrice() {
        Cart mockCart = new Cart();
        mockCart.setTotalPrice(TEST_TOTAL_PRICE);

        when(cartService.updateQuantity(TEST_USER_UUID, TEST_PRODUCT_UUID, 2))
                .thenReturn(Mono.empty());
        when(cartService.get(TEST_USER_UUID))
                .thenReturn(Mono.just(mockCart));

        webTestClient.patch()
                .uri("/cart/update/{productUuid}?quantity=2", TEST_PRODUCT_UUID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BigDecimal.class)
                .isEqualTo(TEST_TOTAL_PRICE);

        verify(cartService).updateQuantity(TEST_USER_UUID, TEST_PRODUCT_UUID, 2);
        verify(cartService).get(TEST_USER_UUID);
    }

    @Test
    void removeFromCart_ShouldReturnUpdatedTotalPrice() {
        Cart mockCart = new Cart();
        mockCart.setTotalPrice(TEST_TOTAL_PRICE);

        when(cartService.removeFromCart(TEST_USER_UUID, TEST_PRODUCT_UUID))
                .thenReturn(Mono.just(mockCart));

        webTestClient.delete()
                .uri("/cart/remove/{productUuid}", TEST_PRODUCT_UUID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BigDecimal.class)
                .isEqualTo(TEST_TOTAL_PRICE);

        verify(cartService).removeFromCart(TEST_USER_UUID, TEST_PRODUCT_UUID);
    }
}