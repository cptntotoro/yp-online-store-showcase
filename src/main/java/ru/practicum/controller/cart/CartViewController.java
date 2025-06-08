package ru.practicum.controller.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.practicum.config.WebAttributes;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.cart.Cart;
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
    public Mono<String> showCart(Model model, @RequestAttribute(WebAttributes.USER_UUID) UUID userUuid) {
        return cartService.get(userUuid)
                .map(cartMapper::cartToCartDto)
                .doOnNext(dto -> model.addAttribute("cart", dto))
                .thenReturn("cart/cart");
    }

    @PostMapping("/remove/{productUuid}")
    public Mono<String> removeFromCart(
            @RequestAttribute(WebAttributes.USER_UUID) UUID userUuid,
            @PathVariable UUID productUuid) {
        return cartService.removeFromCart(userUuid, productUuid)
                .thenReturn("redirect:/cart");
    }

    @PostMapping("/clear")
    public Mono<String> clearCart(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid) {
        return cartService.clear(userUuid)
                .thenReturn("redirect:/cart");
    }
}