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

    /**
     * Создать заказ
     *
     * @param userUuid Идентификатор пользователя
     * @return Заказ
     */
    Order create(UUID userUuid);

    /**
     * Обновить статус заказа
     *
     * @param orderUuid Идентификатор заказа
     * @param newStatus Статус заказа
     * @return Заказ
     */
    @Transactional
    Order updateStatus(UUID orderUuid, OrderStatus newStatus);

    /**
     * Получить заказы пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Список заказов
     */
    List<Order> getUserOrders(UUID userUuid);

    /**
     *
     * @param orderUuid Идентификатор заказа
     * @return Заказ
     */
    Order getByUuid(UUID userUuid, UUID orderUuid);
}
