package ru.practicum.mapper.order;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dao.order.OrderDao;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.dto.order.OrderItemDto;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderItem;
import ru.practicum.model.product.Product;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Маппер заказов
 */
@Mapper(componentModel = "spring", uses = {OrderItemMapper.class, CartMapper.class})
public interface OrderMapper {

    /**
     * Смаппить заказ в DTO заказа
     *
     * @param orderDto Заказ
     * @return DTO заказа
     */
    OrderDto orderToOrderDto(Order orderDto);

    /**
     * Смаппить заказ в DAO заказа
     *
     * @param order Заказ
     * @return DAO заказа
     */
    OrderDao orderToOrderDao(Order order);

    /**
     * Смаппить DAO заказа в заказ
     *
     * @param orderDao DAO заказа
     * @return Заказ
     */
    @Mapping(target = "items", ignore = true)
    Order orderDaoToOrder(OrderDao orderDao);

    /**
     * Смаппить DAO заказа с товарами заказа в заказ
     *
     * @param orderDao DAO заказа
     * @param items Товары заказа
     * @return Заказ
     */
    default Order orderDaoToOrderWithItems(OrderDao orderDao, List<OrderItem> items) {
        Order order = orderDaoToOrder(orderDao);
        order.setItems(items);
        return order;
    }

    /**
     * Смаппить заказ и товары в DTO заказа
     *
     * @param order Заказ
     * @param products Мапа товаров
     * @return DTO заказа
     */
    default OrderDto orderToOrderDtoWithProducts(Order order, Map<UUID, Product> products, ProductMapper productMapper) {
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
