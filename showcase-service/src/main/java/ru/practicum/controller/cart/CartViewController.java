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

    @GetMapping
    public Mono<Rendering> showCart(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid) {
        return cartService.get(userUuid)
                .map(cart -> Rendering.view("cart/cart")
                        .modelAttribute("cart", cartMapper.cartToCartDto(cart))
                        .build());
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