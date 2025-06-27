package ru.practicum.repository.cart;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.cart.CartItemDao;

import java.util.UUID;

/**
 * Репозиторий товаров козины
 */
@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItemDao, UUID> {

    /**
     * Получить товары корзины по идентификатору корзины
     *
     * @param cartUuid Идентификатор корзины
     * @return Список товаров корзины
     */
    @Query("SELECT * FROM cart_items WHERE cart_uuid = :cartUuid")
    Flux<CartItemDao> findByCartUuid(UUID cartUuid);

    /**
     * Удалить товары корзины по идентификатору корзины
     *
     * @param cartUuid Идентификатор корзины
     */
    @Modifying
    @Query("DELETE FROM cart_items WHERE cart_uuid = :cartUuid")
    Mono<Void> deleteByCartUuid(UUID cartUuid);

    /**
     * Удалить товар корзины по идентификатору корзины и идентификтору товара
     *
     * @param cartUuid Идентификатор корзины
     * @param productUuid Идентификатор товара
     * @return DAO товара корзины
     */
    @Modifying
    @Query("DELETE FROM cart_items WHERE cart_uuid = :cartUuid AND product_uuid = :productUuid")
    Mono<Void> deleteByCartUuidAndProductUuid(UUID cartUuid, UUID productUuid);
}
