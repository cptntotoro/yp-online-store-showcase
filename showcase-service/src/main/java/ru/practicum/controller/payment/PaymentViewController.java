package ru.practicum.controller.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.practicum.client.PaymentServiceClient;
import ru.practicum.config.WebAttributes;
import ru.practicum.dto.payment.PaymentCheckoutDto;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.service.cart.CartService;
import ru.practicum.service.order.OrderPaymentService;
import ru.practicum.service.order.OrderService;

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
     * Сервис оплаты заказов
     */
    private final OrderPaymentService orderPaymentService;

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
                        .then(orderPaymentService.checkHealth()
                                .doOnNext(isActive -> {
                                    model.addAttribute("order", orderMapper.orderToOrderDto(order));
                                    model.addAttribute("paymentServiceActive", isActive);
                                })
                                .thenReturn(order)))
                .map(order -> "payment/payment");
    }

    @GetMapping("/checkout/created/{orderUuid}")
    public Mono<String> previewExistingOrder(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid,
                                             @PathVariable UUID orderUuid,
                                             Model model) {
        return orderService.getByUuid(userUuid, orderUuid)
                .flatMap(order -> orderPaymentService.checkHealth()
                        .doOnNext(isActive -> {
                            model.addAttribute("order", orderMapper.orderToOrderDto(order));
                            model.addAttribute("paymentServiceActive", isActive);
                        })
                        .thenReturn(order))
                .map(order -> "payment/payment");
    }

    @PostMapping("/{orderUuid}/checkout")
    public Mono<String> checkout(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid,
                               @PathVariable UUID orderUuid,
                               @ModelAttribute PaymentCheckoutDto paymentCheckoutDto) {
        return orderPaymentService.processPayment(userUuid, orderUuid, paymentCheckoutDto.getCardNumber())
                .thenReturn("redirect:/orders/" + orderUuid);
    }
}
