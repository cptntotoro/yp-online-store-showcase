package ru.practicum.mapper.order;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.order.OrderItemDto;
import ru.practicum.model.order.OrderItem;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    OrderItemMapper INSTANCE = Mappers.getMapper(OrderItemMapper.class);

    OrderItem orderItemToOrderItemDto(OrderItemDto orderItemDto);
}
