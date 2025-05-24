package ru.practicum.mapper.order;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.model.order.Order;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    Order orderToOrderDto(OrderDto orderDto);
}
