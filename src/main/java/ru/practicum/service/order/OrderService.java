package ru.practicum.service.order;

import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrderStatus;

import java.util.List;
import java.util.UUID;

/**
 * Сервис управления заказами
 */
public interface OrderService {
//    Order add(List<CartItem> cartItems);

//    List<Order> getAll();

    Order create(UUID userUuid);

    @Transactional
    Order updateStatus(UUID orderUuid, OrderStatus newStatus);

    List<Order> getUserOrders(UUID userUuid);

    Order getByUuid(UUID uuid);
}
