package ru.practicum.controller.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.practicum.config.WebAttributes;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.model.order.Order;
import ru.practicum.service.cart.CartService;
import ru.practicum.service.order.OrderService;
import ru.practicum.service.payment.PaymentService;

import java.util.UUID;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentViewController {
    /**
     * Сервис управления заказами
     */
    private final OrderService orderService;

    /**
     * Сервис оплаты заказа
     */
    private final PaymentService paymentService;

    /**
     * Сервис управления корзиной товаров
     */
    private final CartService cartService;

    /**
     * Маппер заказов
     */
    private final OrderMapper orderMapper;

    @GetMapping("/checkout")
    public String previewOrder(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, Model model) {
        Order order = orderService.create(userUuid);
        cartService.clear(userUuid);
        model.addAttribute("order", orderMapper.orderToOrderDto(order));
        return "payment/payment";
    }

    @PostMapping("/checkout/{orderUuid}")
    public String checkout(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, @PathVariable UUID orderUuid,
                           @RequestParam String cardNumber) {
        paymentService.checkout(userUuid, orderUuid, cardNumber);
        return "redirect:/orders/" + orderUuid;
    }
}
