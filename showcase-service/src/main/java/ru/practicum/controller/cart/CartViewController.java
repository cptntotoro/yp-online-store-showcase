package ru.practicum.controller.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.RedirectView;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.user.User;
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

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public Mono<Rendering> showCart(@AuthenticationPrincipal User user) {
        return cartService.get(user.getUuid())
                .map(cart -> Rendering.view("cart/cart")
                        .modelAttribute("cart", cartMapper.cartToCartDto(cart))
                        .build());
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/remove/{productUuid}")
    public Mono<RedirectView> removeFromCart(@AuthenticationPrincipal User user,
                                             @PathVariable UUID productUuid) {
        return cartService.removeFromCart(user.getUuid(), productUuid)
                .thenReturn(new RedirectView("/cart"));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/clear")
    public Mono<RedirectView> clearCart(@AuthenticationPrincipal User user) {
        return cartService.clear(user.getUuid())
                .thenReturn(new RedirectView("/cart"));
    }
}