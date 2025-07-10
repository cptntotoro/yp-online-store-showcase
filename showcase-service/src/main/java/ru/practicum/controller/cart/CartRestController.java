package ru.practicum.controller.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

//    @PreAuthorize("#user.username == authentication.name")
//  @PreAuthorize("#product.ownerId == principal.id")
    @PostMapping("/add/{productUuid}")
    public Mono<BigDecimal> addToCart(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid,
                                      @PathVariable UUID productUuid,
                                      @RequestParam int quantity) {
        return cartService.addToCart(userUuid, productUuid, quantity)
                .map(Cart::getTotalPrice);
    }

//    @PreAuthorize("#user.username == authentication.name")
//  @PreAuthorize("#product.ownerId == principal.id")
    @PatchMapping("/update/{productUuid}")
    public Mono<BigDecimal> updateCartItem(@PathVariable UUID productUuid,
                                           @RequestParam int quantity,
                                           @RequestAttribute(WebAttributes.USER_UUID) UUID userUuid) {
        return cartService.updateQuantity(userUuid, productUuid, quantity)
                .then(cartService.get(userUuid))
                .map(Cart::getTotalPrice);
    }

//    @PreAuthorize("#user.username == authentication.name")
//  @PreAuthorize("#product.ownerId == principal.id")
    @DeleteMapping("/remove/{productUuid}")
    public Mono<BigDecimal> removeFromCart(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid,
                                           @PathVariable UUID productUuid) {
        return cartService.removeFromCart(userUuid, productUuid)
                .map(Cart::getTotalPrice);
    }
}
