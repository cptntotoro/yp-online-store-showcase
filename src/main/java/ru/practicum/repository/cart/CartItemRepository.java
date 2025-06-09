package ru.practicum.repository.cart;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.cart.CartItemDao;

import java.util.UUID;

/**
 * Репозиторий товаров козины
 */
public interface CartItemRepository extends ReactiveCrudRepository<CartItemDao, UUID> {
    @Query("SELECT * FROM cart_items WHERE cart_uuid = :cartUuid")
    Flux<CartItemDao> findByCartUuid(UUID cartUuid);

    @Query("SELECT * FROM cart_items WHERE product_uuid = :productUuid")
    Flux<CartItemDao> findByProductUuid(UUID productUuid);

    @Modifying
    @Query("DELETE FROM cart_items WHERE cart_uuid = :cartUuid")
    Mono<Void> deleteByCartUuid(UUID uuid);

    @Modifying
    @Query("DELETE FROM cart_items WHERE cart_uuid = :cartUuid AND product_uuid = :productUuid")
    Mono<CartItemDao> deleteByCartUuidAndProductUuid(UUID uuid, UUID productUuid);
}
