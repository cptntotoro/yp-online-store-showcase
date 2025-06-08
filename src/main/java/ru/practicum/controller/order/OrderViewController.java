package ru.practicum.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.practicum.config.WebAttributes;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.model.order.Order;
import ru.practicum.service.order.OrderService;

import java.math.BigDecimal;
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
    public Mono<String> showOrderList(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, Model model) {
        Mono<List<Order>> ordersMono = orderService.getUserOrders(userUuid).collectList();
        Mono<BigDecimal> totalAmountMono = orderService.getUserTotalAmount(userUuid);

        return Mono.zip(ordersMono, totalAmountMono)
                .doOnNext(tuple -> {
                    List<OrderDto> orderDtos = tuple.getT1().stream().map(orderMapper::orderToOrderDto).toList();
                    model.addAttribute("orders", orderDtos);
                    model.addAttribute("hasOrders", !orderDtos.isEmpty());
                    model.addAttribute("cartTotal", tuple.getT2());
                })
                .thenReturn("order/orders");
    }

    @GetMapping("/{orderUuid}")
    public Mono<String> showOrderDetails(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, @PathVariable UUID orderUuid, Model model) {
        return orderService.getByUuid(userUuid, orderUuid)
                .map(orderMapper::orderToOrderDto)
                .doOnNext(dto -> model.addAttribute("order", dto))
                .thenReturn("order/order");
    }

    @GetMapping("/checkout/cancel/{orderUuid}")
    public Mono<String> cancel(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, @PathVariable UUID orderUuid) {
        return orderService.cancel(userUuid, orderUuid)
                .thenReturn("redirect:/orders/" + orderUuid);
    }
}