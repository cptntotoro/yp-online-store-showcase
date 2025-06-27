package ru.practicum.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.practicum.config.WebAttributes;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.mapper.order.OrderMapper;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderItem;
import ru.practicum.service.order.OrderPaymentService;
import ru.practicum.service.order.OrderService;
import ru.practicum.service.product.ProductService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderViewController {
    /**
     * Сервис управления заказами
     */
    private final OrderService orderService;

    /**
     * Сервис обработки заказов
     */
    private final OrderPaymentService orderPaymentService;

    /**
     * Сервис управления товарами
     */
    private final ProductService productService;

    /**
     * Маппер заказов
     */
    private final OrderMapper orderMapper;

    /**
     * Маппер товаров
     */
    private final ProductMapper productMapper;

    @GetMapping
    public Mono<String> showOrderList(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, Model model) {
        return orderService.getUserOrdersWithProducts(userUuid)
                .map(data -> {
                    List<OrderDto> orderDtos = data.getOrders().stream()
                            .map(order -> orderMapper.orderToOrderDtoWithProducts(order, data.getProducts(), productMapper))
                            .collect(Collectors.toList());

                    model.addAttribute("orders", orderDtos);
                    model.addAttribute("hasOrders", !orderDtos.isEmpty());
                    model.addAttribute("cartTotal", data.getTotalAmount());

                    return "order/orders";
                });
    }

    @GetMapping("/{orderUuid}")
    public Mono<String> showOrderDetails(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid,
                                         @PathVariable UUID orderUuid,
                                         Model model) {
        return Mono.zip(
                orderService.getByUuid(userUuid, orderUuid),
                orderPaymentService.checkHealth().defaultIfEmpty(false)
        ).flatMap(tuple -> {
            Order order = tuple.getT1();
            Boolean isServiceActive = tuple.getT2();

            Set<UUID> productUuids = order.getItems().stream()
                    .map(OrderItem::getProductUuid)
                    .collect(Collectors.toSet());

            return productService.getProductsByUuids(productUuids)
                    .map(productsMap -> {
                        OrderDto dto = orderMapper.orderToOrderDtoWithProducts(order, productsMap, productMapper);
                        model.addAttribute("order", dto);
                        model.addAttribute("paymentServiceActive", isServiceActive);
                        return "order/order";
                    });
        });
    }

    @GetMapping("/{orderUuid}/checkout/cancel")
    public Mono<String> cancel(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, @PathVariable UUID orderUuid) {
        return orderPaymentService.cancel(userUuid, orderUuid)
                .thenReturn("redirect:/orders/" + orderUuid);
    }
}