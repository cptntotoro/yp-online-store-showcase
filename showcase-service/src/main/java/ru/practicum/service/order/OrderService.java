package ru.practicum.service.order;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.order.Order;
import ru.practicum.model.order.OrdersWithTotal;

import java.math.BigDecimal;
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
    Mono<Order> create(UUID userUuid);

    /**
     * Сохранить заказ
     *
     * @param order Заказ
     * @return Заказ
     */
    Mono<Order> save(Order order);

    /**
     * Получить заказы пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Список заказов
     */
    Flux<Order> getUserOrders(UUID userUuid);

    /**
     * Получить заказ
     *
     * @param userUuid  Идентификатор пользователя
     * @param orderUuid Идентификатор заказа
     * @return Заказ
     */
    Mono<Order> getByUuid(UUID userUuid, UUID orderUuid);

    /**
     * Получить стоимость всех заказов пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Стоимость всех заказов пользователя
     */
    Mono<BigDecimal> getUserTotalAmount(UUID userUuid);

    /**
     * Получить список заказов пользователя с информацией о товарах
     *
     * @param userUuid идентификатор пользователя
     * @return список заказов с продуктами и общей суммой
     */
    Mono<OrdersWithTotal> getUserOrdersWithProducts(UUID userUuid);
}
