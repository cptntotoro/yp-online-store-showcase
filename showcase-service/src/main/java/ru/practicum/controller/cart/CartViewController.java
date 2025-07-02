package ru.practicum.controller.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.practicum.config.WebAttributes;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.service.cart.CartService;
import ru.practicum.service.order.OrderPaymentService;

import java.util.UUID;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartViewController {
    /**
     * Сервис управления корзиной товаров
     */
    private final CartService cartService;

    /**
     * Маппер корзины товаров
     */
    private final CartMapper cartMapper;

    /**
     * Сервис оплаты заказов
     */
    private final OrderPaymentService orderPaymentService;

    @GetMapping
    public Mono<Rendering> showCart(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid) {
        return cartService.get(userUuid)
                .flatMap(cart -> orderPaymentService.checkHealth()
                        .defaultIfEmpty(false)
                        .map(isServiceActive -> Rendering.view("cart/cart")
                                .modelAttribute("cart", cartMapper.cartToCartDto(cart))
                                .modelAttribute("paymentServiceActive", isServiceActive)
                                .build()));
    }

    @PostMapping("/remove/{productUuid}")
    public Mono<RedirectView> removeFromCart(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid,
                                             @PathVariable UUID productUuid) {
        return cartService.removeFromCart(userUuid, productUuid)
                .thenReturn(new RedirectView("/cart"));
    }

    @PostMapping("/clear")
    public Mono<RedirectView> clearCart(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid) {
        return cartService.clear(userUuid)
                .thenReturn(new RedirectView("/cart"));
    }
}