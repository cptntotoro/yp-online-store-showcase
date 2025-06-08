package ru.practicum.repository.cart;

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
    Mono<CartDao> findByUserUuid(UUID userUuid);
}
