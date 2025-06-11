package ru.practicum.mapper.order;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dao.order.OrderDao;
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

}
