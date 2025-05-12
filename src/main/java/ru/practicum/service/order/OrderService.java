package ru.practicum.service.order;

import ru.practicum.model.order.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Сервис управления заказами
 */
public interface OrderService {

    /**
     * Создать заказ
     *
     * @param userUuid Идентификатор пользователя
     * @return Заказ
     */
    Order create(UUID userUuid);

    /**
     * Получить заказы пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Список заказов
     */
    List<Order> getUserOrders(UUID userUuid);

    /**
     * Получить заказ
     *
     * @param userUuid Идентификатор пользователя
     * @param orderUuid Идентификатор заказа
     * @return Заказ
     */
    Order getByUuid(UUID userUuid, UUID orderUuid);

    /**
     * Оплатить заказ
     *
     * @param userUuid Идентификатор пользователя
     * @param orderUuid Идентификатор заказа
     */
    void checkout(UUID userUuid, UUID orderUuid);

    /**
     * Отменить заказ
     *
     * @param userUuid Идентификатор пользователя
     * @param orderUuid Идентификатор заказа
     */
    void cancel(UUID userUuid, UUID orderUuid);

    /**
     * Получить стоимость всех заказов пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Стоимость всех заказов пользователя
     */
    BigDecimal getUserTotalAmount(UUID userUuid);
}
