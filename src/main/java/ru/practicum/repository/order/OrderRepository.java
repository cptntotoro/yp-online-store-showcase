package ru.practicum.repository.order;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.order.OrderDao;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Репозиторий заказов
 */
@Repository
public interface OrderRepository extends ReactiveCrudRepository<OrderDao, UUID> {

    /**
     * Получить список заказов по идентификатору пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return DAO заказа
     */
    @Query("SELECT * FROM orders WHERE user_uuid = :userUuid")
    Flux<OrderDao> findByUserUuid(UUID userUuid);

    /**
     * Получить заказ по идентификатору заказа и идентификатору пользователя
     *
     * @param orderUuid Идентификатор заказа
     * @param userUuid Идентификатор пользователя
     * @return DAO заказа
     */
    @Query("SELECT * FROM orders WHERE order_uuid = :orderUuid AND user_uuid = :userUuid")
    Mono<OrderDao> findByUuidAndUserUuid(UUID orderUuid, UUID userUuid);

    /**
     * Получить стоимость заказов пользователя по идентификатору пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Стоимость заказов пользователя
     */
    @Query("SELECT COALESCE(SUM(total_amount), 0) FROM orders WHERE user_uuid = :userUuid")
    Mono<BigDecimal> getTotalOrdersAmountByUser(UUID userUuid);
}
