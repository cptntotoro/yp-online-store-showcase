package ru.practicum.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.model.CartItem;
import ru.practicum.model.Order;
import ru.practicum.service.cart.ShoppingCartService;
import ru.practicum.service.order.OrderService;
import ru.practicum.service.product.ProductService;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderViewController {
    private final OrderService orderService;
    private final ShoppingCartService cartService;
    private final ProductService productService;

    @GetMapping
    public String showOrderList(Model model) {
        List<Order> orders = orderService.getAll();
        model.addAttribute("orders", orders);
        return "order/orders";
    }

    @GetMapping("/{uuid}")
    public String showOrderDetails(@PathVariable UUID uuid, Model model) {
        Order order = orderService.getByUuid(uuid);
        model.addAttribute("order", order);
        return "orders/details";
    }

    @PostMapping("/checkout")
    public String checkout() {
        List<CartItem> cartItems = cartService.getAll();
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        Order order = orderService.add(cartItems);
        cartService.clear();
        return "redirect:/orders/" + order.getUuid();
    }
}