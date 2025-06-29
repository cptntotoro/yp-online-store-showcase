package ru.practicum.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.practicum.service.order.OrderPaymentService;

import java.util.UUID;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderRestController {

    /**
     * Сервис оплаты заказа
     */
    private final OrderPaymentService orderPaymentService;

    @GetMapping("/{orderId}/check-availability")
    public Mono<Boolean> checkOrderAvailable(@PathVariable UUID orderId, @RequestParam UUID userId) {
        return orderPaymentService.isBalanceSufficient(userId, orderId);
    }
}
