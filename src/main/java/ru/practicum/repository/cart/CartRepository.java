package ru.practicum.repository.cart;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.practicum.dao.cart.CartDao;

import java.util.UUID;

/**
 * Репозиторий корзины товаров
 */
@Repository
public interface CartRepository extends ReactiveCrudRepository<CartDao, UUID> {

    /**
     * Получить корзину по идентификатору пользователя
     *
     * @param userUuid Идентификатор пользователя
     */
    @Query("SELECT * FROM carts WHERE user_uuid = :userUuid")
    Mono<CartDao> findByUserUuid(UUID userUuid);
}
