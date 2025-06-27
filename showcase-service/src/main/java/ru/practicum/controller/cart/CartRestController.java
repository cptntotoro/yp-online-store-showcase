package ru.practicum.controller.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.practicum.config.WebAttributes;
import ru.practicum.model.cart.Cart;
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

    @PostMapping("/add/{productUuid}")
    public Mono<BigDecimal> addToCart(
            @RequestAttribute(WebAttributes.USER_UUID) UUID userUuid,
            @PathVariable UUID productUuid,
            @RequestParam int quantity) {
        return cartService.addToCart(userUuid, productUuid, quantity)
                .map(Cart::getTotalPrice);
    }

    @PatchMapping("/update/{productUuid}")
    public Mono<BigDecimal> updateCartItem(
            @PathVariable UUID productUuid,
            @RequestParam int quantity,
            @RequestAttribute(WebAttributes.USER_UUID) UUID userUuid) {
        return cartService.updateQuantity(userUuid, productUuid, quantity)
                .then(cartService.get(userUuid))
                .map(Cart::getTotalPrice);
    }

    @DeleteMapping("/remove/{productUuid}")
    public Mono<BigDecimal> removeFromCart(
            @PathVariable UUID productUuid,
            @RequestAttribute(WebAttributes.USER_UUID) UUID userUuid) {
        return cartService.removeFromCart(userUuid, productUuid)
                .map(Cart::getTotalPrice);
    }
}
