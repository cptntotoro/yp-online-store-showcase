package ru.practicum.controller.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.practicum.config.WebAttributes;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.mapper.order.OrderDtoMapper;
import ru.practicum.model.order.OrderItem;
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
     * Сервис управления товарами
     */
    private final ProductService productService;

    /**
     * Маппер DTO заказов
     */
    private final OrderDtoMapper orderDtoMapper;

    @GetMapping
    public Mono<String> showOrderList(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, Model model) {
        return orderService.getUserOrdersWithProducts(userUuid)
                .map(data -> {
                    List<OrderDto> orderDtos = data.getOrders().stream()
                            .map(order -> orderDtoMapper.orderAssignProductsToOrderDto(order, data.getProducts()))
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

        return orderService.getByUuid(userUuid, orderUuid)
                .flatMap(order -> {
                    Set<UUID> productIds = order.getItems().stream()
                            .map(OrderItem::getProductUuid)
                            .collect(Collectors.toSet());

                    return productService.getProductsByIds(productIds)
                            .map(productsMap -> {
                                OrderDto dto = orderDtoMapper.orderAssignProductsToOrderDto(order, productsMap);
                                model.addAttribute("order", dto);
                                return "order/order";
                            });
                });
    }

    @GetMapping("/checkout/cancel/{orderUuid}")
    public Mono<String> cancel(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid, @PathVariable UUID orderUuid) {
        return orderService.cancel(userUuid, orderUuid)
                .thenReturn("redirect:/orders/" + orderUuid);
    }
}