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
    public String showCart(Model model, @RequestAttribute(WebAttributes.USER_UUID) String userUuid) {
        model.addAttribute("cart", cartService.get(UUID.fromString(userUuid)));
        return "cart/cart";
    }

    @PostMapping("/add/{productUuid}")
    public String addToCart(
            @RequestAttribute(WebAttributes.USER_UUID) String userUuid,
            @PathVariable UUID productUuid,
            @RequestParam int quantity) {
        cartService.addToCart(UUID.fromString(userUuid), productUuid, quantity);
        return "redirect:/products/" + productUuid;
    }

    @PostMapping("/remove/{productUuid}")
    public String removeFromCart(
            @RequestAttribute(WebAttributes.USER_UUID) String userUuid,
            @PathVariable UUID productUuid) {
        cartService.removeFromCart(UUID.fromString(userUuid), productUuid);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(@RequestAttribute(WebAttributes.USER_UUID) String userUuid) {
        cartService.clear(UUID.fromString(userUuid));
        return "redirect:/cart";
    }
}