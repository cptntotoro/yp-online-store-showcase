package ru.practicum.controller.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.config.WebAttributes;
import ru.practicum.service.cart.CartServiceImpl;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartRestController {
    /**
     * Сервис управления корзиной товаров
     */
    private final CartServiceImpl cartService;

    @PostMapping("/add/{productUuid}")
    public BigDecimal addToCart(
            @RequestAttribute(WebAttributes.USER_UUID) UUID userUuid,
            @PathVariable UUID productUuid,
            @RequestParam int quantity) {
        return cartService.addToCart(userUuid, productUuid, quantity).getTotalPrice();
    }

    @PatchMapping("/update/{productUuid}")
    public BigDecimal updateCartItem(
            @PathVariable UUID productUuid,
            @RequestParam int quantity,
            @RequestAttribute(WebAttributes.USER_UUID) UUID userUuid) {
        cartService.updateQuantity(userUuid, productUuid, quantity);
        return cartService.getCachedCart(userUuid).getTotalPrice();
    }

    @DeleteMapping("/remove/{productUuid}")
    public BigDecimal removeFromCart(
            @PathVariable UUID productUuid,
            @RequestAttribute(WebAttributes.USER_UUID) UUID userUuid) {
        cartService.removeFromCart(userUuid, productUuid);
        return cartService.getCachedCart(userUuid).getTotalPrice();
    }
}
