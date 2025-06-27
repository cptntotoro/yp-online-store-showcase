package ru.practicum.mapper.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.dto.order.OrderItemDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.order.Order;
import ru.practicum.model.product.Product;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Маппер DTO заказов
 */
@Component
@RequiredArgsConstructor
public class OrderDtoMapper {

    /**
     * Маппер товаров
     */
    private final ProductMapper productMapper;

    public OrderDto orderAssignProductsToOrderDto(Order order, Map<UUID, Product> products) {
        List<OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> {
                    Product product = products.get(item.getProductUuid());
                    return OrderItemDto.builder()
                            .uuid(item.getUuid())
                            .product(product != null ?
                                    productMapper.productToProductOutDto(product) : null)
                            .quantity(item.getQuantity())
                            .priceAtOrder(item.getPriceAtOrder())
                            .build();
                })
                .collect(Collectors.toList());

        return OrderDto.builder()
                .uuid(order.getUuid())
                .status(order.getStatus())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .items(itemDtos)
                .build();
    }
}
