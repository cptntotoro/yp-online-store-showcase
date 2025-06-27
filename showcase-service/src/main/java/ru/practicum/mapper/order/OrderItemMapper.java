package ru.practicum.mapper.order;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dao.order.OrderItemDao;
import ru.practicum.dto.order.OrderItemDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.order.OrderItem;

/**
 * Маппер товаров заказа
 */
@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface OrderItemMapper {

    /**
     * Смаппить товар заказа в DTO товара заказа
     *
     * @param orderItem Товар заказа
     * @return DTO товара заказа
     */
    @Mapping(source = "productUuid", target = "product", ignore = true)
    OrderItemDto orderItemToOrderItemDto(OrderItem orderItem);

    /**
     * Смаппить товар заказа в DAO товара заказа
     *
     * @param orderItem Товар заказа
     * @return DAO товара заказа
     */
    OrderItemDao orderItemToOrderItemDao(OrderItem orderItem);
}
