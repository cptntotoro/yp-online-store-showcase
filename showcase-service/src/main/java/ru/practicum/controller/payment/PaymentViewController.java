package ru.practicum.controller.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.practicum.dto.payment.PaymentCheckoutDto;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.model.order.Order;
import ru.practicum.model.user.User;
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

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/checkout")
    public Mono<String> previewOrder(@AuthenticationPrincipal User user, Model model) {
        return Mono.zip(
                        orderService.create(user.getUuid())
                                .flatMap(order -> cartService.clear(user.getUuid())
                                        .thenReturn(order)),
                        orderPaymentService.checkHealth().defaultIfEmpty(false)
                )
                .doOnNext(tuple -> {
                    model.addAttribute("order", orderMapper.orderToOrderDto(tuple.getT1()));
                    model.addAttribute("paymentServiceActive", tuple.getT2());
                })
                .map(tuple -> "payment/payment");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/checkout/created/{orderUuid}")
    public Mono<String> previewExistingOrder(@AuthenticationPrincipal User user,
                                             @PathVariable UUID orderUuid,
                                             Model model) {
        return Mono.zip(
                        orderService.getByUuid(user.getUuid(), orderUuid),
                        orderPaymentService.checkHealth().defaultIfEmpty(false)
                )
                .doOnNext(tuple -> {
                    model.addAttribute("order", orderMapper.orderToOrderDto(tuple.getT1()));
                    model.addAttribute("paymentServiceActive", tuple.getT2());
                })
                .map(tuple -> "payment/payment");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{orderUuid}/checkout")
    public Mono<String> checkout(@AuthenticationPrincipal User user,
                                 @PathVariable UUID orderUuid,
                                 @ModelAttribute PaymentCheckoutDto paymentCheckoutDto,
                                 Model model) {
        return Mono.zip(
                        orderService.getByUuid(user.getUuid(), orderUuid),
                        orderPaymentService.checkHealth().defaultIfEmpty(false)
                )
                .flatMap(tuple -> {
                    Order order = tuple.getT1();
                    boolean isServiceActive = tuple.getT2();

                    model.addAttribute("order", orderMapper.orderToOrderDto(order));
                    model.addAttribute("paymentServiceActive", isServiceActive);

                    if (!isServiceActive) {
                        return Mono.just("payment/payment");
                    }

                    return orderPaymentService.isBalanceSufficient(user.getUuid(), order.getUuid())
                            .defaultIfEmpty(false)
                            .flatMap(isBalanceSufficient -> {
                                model.addAttribute("balanceSufficient", isBalanceSufficient);

                                if (!isBalanceSufficient) {
                                    return Mono.just("payment/payment");
                                }

                                return orderPaymentService.processPayment(user.getUuid(), orderUuid, paymentCheckoutDto.getCardNumber())
                                        .thenReturn("redirect:/orders/" + orderUuid);
                            });
                });
    }
}
