package ru.practicum.controller.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    public String showCart(Model model, @RequestAttribute(WebAttributes.USER_UUID) UUID userUuid) {
        Cart cart = cartService.get(userUuid);
        model.addAttribute("cart", cartMapper.cartToCartDto(cart));
        return "cart/cart";
    }

    @PostMapping("/remove/{productUuid}")
    public String removeFromCart(
            @RequestAttribute(WebAttributes.USER_UUID) UUID userUuid,
            @PathVariable UUID productUuid) {
        cartService.removeFromCart(userUuid, productUuid);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid) {
        cartService.clear(userUuid);
        return "redirect:/cart";
    }
}