package ru.practicum.service.order;

import ru.practicum.model.CartItem;
import ru.practicum.model.Order;

import java.util.List;
import java.util.UUID;

/**
 * Сервис управления заказами
 */
public interface OrderService {
    Order add(List<CartItem> cartItems);

    List<Order> getAll();

    Order getByUuid(UUID uuid);
}
