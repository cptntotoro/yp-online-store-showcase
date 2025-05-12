package ru.practicum.controller.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.Product;
import ru.practicum.service.product.ProductServiceImpl;
import ru.practicum.service.cart.ShoppingCartServiceImpl;

import java.util.UUID;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartViewController {
    private final ShoppingCartServiceImpl cartService;
    private final ProductServiceImpl productService;

    @GetMapping
    public String showCart(Model model) {
        model.addAttribute("items", cartService.getAll());
        model.addAttribute("total", cartService.getTotalPrice());
        return "cart/cart";
    }

    @PostMapping("/add/{productUuid}")
    public String addToCart(@PathVariable UUID productUuid, @RequestParam int quantity) {
        Product product = productService.getByUuid(productUuid);
        cartService.add(product, quantity);
        return "redirect:/products/" + productUuid;
    }

    @PostMapping("/update/{productUuid}")
    public String updateCartItem(@PathVariable UUID productUuid, @RequestParam int quantity) {
        cartService.updateQuantity(productUuid, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{productUuid}")
    public String removeFromCart(@PathVariable UUID productUuid) {
        cartService.remove(productUuid);
        return "redirect:/cart";
    }
}