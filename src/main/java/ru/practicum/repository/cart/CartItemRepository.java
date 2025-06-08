package ru.practicum.repository.cart;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.practicum.dao.cart.CartItemDao;

import java.util.UUID;

public interface CartItemRepository extends ReactiveCrudRepository<CartItemDao, UUID> {
    Flux<CartItemDao> findByCartUuid(UUID cartUuid);
    Flux<CartItemDao> findByProductUuid(UUID productUuid);
}
