package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.model.CartItem;
import ru.practicum.model.Order;
import ru.practicum.service.OrderService;
import ru.practicum.service.ProductService;
import ru.practicum.service.ShoppingCartService;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final ShoppingCartService cartService;
    private final ProductService productService;

    @GetMapping
    public String showOrderList(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String showOrderDetails(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        model.addAttribute("order", order);
        return "orders/details";
    }

    @PostMapping("/checkout")
    public String checkout() {
        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        Order order = orderService.createOrder(cartItems);
        cartService.clearCart();
        return "redirect:/orders/" + order.getId();
    }
}