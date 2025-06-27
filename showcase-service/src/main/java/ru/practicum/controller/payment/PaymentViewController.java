package ru.practicum.controller.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.practicum.config.WebAttributes;
import ru.practicum.dto.payment.PaymentCheckoutDto;
import ru.practicum.mapper.order.OrderMapper;
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
    public Mono<String> previewOrder(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, Model model) {
        return orderService.create(userUuid)
                .flatMap(order -> cartService.clear(userUuid)
                        .thenReturn(order))
                .map(order -> {
                    model.addAttribute("order", orderMapper.orderToOrderDto(order));
                    return "payment/payment";
                });
    }

    @GetMapping("/checkout/created/{orderUuid}")
    public Mono<String> previewExistingOrder(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, @PathVariable UUID orderUuid, Model model) {
        return orderService.getByUuid(userUuid, orderUuid)
                .map(order -> {
                    model.addAttribute("order", orderMapper.orderToOrderDto(order));
                    return "payment/payment";
                });
    }

    @PostMapping("/checkout/{orderUuid}")
    public Mono<String> checkout(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, @PathVariable UUID orderUuid,
                                 @ModelAttribute PaymentCheckoutDto paymentCheckoutDto) {
        return paymentService.checkout(userUuid, orderUuid, paymentCheckoutDto.getCardNumber())
                .thenReturn("redirect:/orders/" + orderUuid);
    }
}
