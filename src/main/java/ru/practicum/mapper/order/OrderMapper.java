package ru.practicum.mapper.order;

import org.mapstruct.Mapper;
import ru.practicum.dto.order.OrderDto;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.order.Order;

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
}
