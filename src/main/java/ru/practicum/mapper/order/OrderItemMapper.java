package ru.practicum.mapper.order;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import ru.practicum.dto.order.OrderItemDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.order.OrderItem;

@Mapper(componentModel = "spring", uses = ProductMapper.class)
public interface OrderItemMapper {

    /**
     * Смаппить товар заказа в DTO товара заказа
     * @param orderItem Товар заказа
     * @return DTO товара заказа
     */
    @Mapping(source = "product", target = "product")
    OrderItemDto orderItemToOrderItemDto(OrderItem orderItem);
}
