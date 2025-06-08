package ru.practicum.repository.order;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.practicum.dao.order.OrderItemDao;

import java.util.UUID;

/**
 * Репозиторий товаров заказа
 */
@Repository
public interface OrderItemRepository extends ReactiveCrudRepository<OrderItemDao, UUID> {

    /**
     * Получить товары заказа по идентификатору заказа
     *
     * @param orderUuid Идентификатор заказа
     * @return DAO товара заказа
     */
    @Query("SELECT * FROM order_items WHERE order_uuid = :orderUuid")
    Flux<OrderItemDao> findByOrderUuid(UUID orderUuid);
}
