package ru.practicum.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.practicum.config.WebAttributes;
import ru.practicum.model.order.Order;
import ru.practicum.service.cart.CartService;
import ru.practicum.service.order.OrderService;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderViewController {
    private final OrderService orderService;
    private final CartService cartService;

    @GetMapping
    public String showOrderList(@RequestAttribute(WebAttributes.USER_UUID) String userUuid, Model model) {
        List<Order> orders = orderService.getUserOrders(UUID.fromString(userUuid));
        model.addAttribute("orders", orders);
        return "order/orders";
    }

    @GetMapping("/{orderUuid}")
    public String showOrderDetails(@PathVariable UUID orderUuid, Model model) {
        Order order = orderService.getByUuid(orderUuid);
        model.addAttribute("order", order);
        return "orders/order";
    }

    @PostMapping("/checkout")
    public String checkout(@RequestAttribute(WebAttributes.USER_UUID) String userUuid) {
//        Cart cart = cartService.get(UUID.fromString(userUuid));
//        if (cart.getItems().isEmpty()) {
//            return "redirect:/cart";
//        }
//
        Order order = orderService.create(UUID.fromString(userUuid));
        cartService.clear(UUID.fromString(userUuid));
        return "redirect:/orders/" + order.getUuid();
    }
}