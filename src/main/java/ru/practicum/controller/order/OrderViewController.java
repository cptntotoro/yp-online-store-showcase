package ru.practicum.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.practicum.config.WebAttributes;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.model.order.Order;
import ru.practicum.service.order.OrderService;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderViewController {
    /**
     * Сервис управления заказами
     */
    private final OrderService orderService;

    /**
     * Маппер заказов
     */
    private final OrderMapper orderMapper;

    @GetMapping
    public String showOrderList(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, Model model) {
        List<Order> orders = orderService.getUserOrders(userUuid);
        model.addAttribute("orders", orders.stream().map(orderMapper::orderToOrderDto).toList());
        model.addAttribute("hasOrders", !orders.isEmpty());
        model.addAttribute("cartTotal", orderService.getUserTotalAmount(userUuid));
        return "order/orders";
    }

    @GetMapping("/{orderUuid}")
    public String showOrderDetails(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, @PathVariable UUID orderUuid, Model model) {
        Order order = orderService.getByUuid(userUuid, orderUuid);
        model.addAttribute("order", orderMapper.orderToOrderDto(order));
        return "order/order";
    }

    @GetMapping("/checkout/cancel/{orderUuid}")
    public String cancel(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, @PathVariable UUID orderUuid) {
        orderService.cancel(userUuid, orderUuid);
        return "redirect:/orders/" + orderUuid;
    }
}