package ru.practicum.repository.order;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.practicum.dao.order.OrderItemDao;

import java.util.UUID;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItemDao, UUID> {
    Flux<OrderItemDao> findByOrderUuid(UUID orderUuid);
}
