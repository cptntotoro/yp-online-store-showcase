package ru.practicum.controller.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.practicum.config.WebAttributes;
import ru.practicum.service.cart.CartServiceImpl;

import java.util.UUID;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartViewController {
    private final CartServiceImpl cartService;

    @GetMapping
    public String showCart(Model model, @RequestAttribute(WebAttributes.USER_UUID) UUID userUuid) {
        model.addAttribute("cart", cartService.get(userUuid));
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