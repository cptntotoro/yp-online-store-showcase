package ru.practicum.controller.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.user.User;
import ru.practicum.service.cart.CartService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartRestController {
    /**
     * Сервис управления корзиной товаров
     */
    private final CartService cartService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add/{productUuid}")
    public Mono<BigDecimal> addToCart(@AuthenticationPrincipal User user,
                                      @PathVariable UUID productUuid,
                                      @RequestParam int quantity) {
        return cartService.addToCart(user.getUuid(), productUuid, quantity)
                .map(Cart::getTotalPrice);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/update/{productUuid}")
    public Mono<BigDecimal> updateCartItem(@AuthenticationPrincipal User user,
                                           @PathVariable UUID productUuid,
                                           @RequestParam int quantity) {
        return cartService.updateQuantity(user.getUuid(), productUuid, quantity)
                .then(cartService.get(user.getUuid()))
                .map(Cart::getTotalPrice);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/remove/{productUuid}")
    public Mono<BigDecimal> removeFromCart(@AuthenticationPrincipal User user,
                                           @PathVariable UUID productUuid) {
        return cartService.removeFromCart(user.getUuid(), productUuid)
                .map(Cart::getTotalPrice);
    }
}
