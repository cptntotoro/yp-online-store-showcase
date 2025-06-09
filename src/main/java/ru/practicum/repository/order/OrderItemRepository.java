package ru.practicum.repository.order;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.practicum.dao.order.OrderItemDao;

import java.util.UUID;

/**
 * Репозиторий товаров заказа
 */
public interface OrderItemRepository extends ReactiveCrudRepository<OrderItemDao, UUID> {
    @Query("SELECT * FROM order_items WHERE order_uuid = :orderUuid")
    Flux<OrderItemDao> findByOrderUuid(UUID orderUuid);
}
