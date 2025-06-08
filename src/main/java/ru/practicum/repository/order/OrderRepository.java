package ru.practicum.repository.order;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.practicum.dao.order.OrderDao;

import java.util.UUID;

/**
 * Репозиторий заказов
 */
@Repository
public interface OrderRepository extends ReactiveCrudRepository<OrderDao, UUID> {

    Flux<OrderDao> findByOrderUuid(UUID orderUuid);
//    /**
//     * Получить список заказов по идентификатору пользователя
//     *
//     * @param userUuid Идентификатор пользователя
//     * @return Список заказов
//     */
//    Flux<OrderDao> findByUserUuid(UUID userUuid);
//
//    /**
//     * Получить заказ пользователя по идентификаторам заказа и пользователя
//     *
//     * @param orderUuid Идентификатор заказа
//     * @param userUuid Идентификатор пользователя
//     * @return Optional с заказом, если найден
//     */
//    Mono<OrderDao> findByUuidAndUserUuid(UUID orderUuid, UUID userUuid);
//
//    /**
//     * Получить заказ пользователя
//     *
//     * @param uuid Идентификатор заказа
//     * @param userUuid Идентификатор пользователя
//     * @return Optional с заказом, если найден
//     */
//    @Query("SELECT o FROM Order o WHERE o.uuid = :uuid AND o.userUuid = :userUuid")
//    Mono<OrderDao> findByIdWhereUserUuidIn(@Param("uuid") UUID uuid, @Param("userUuid") UUID userUuid);
//
//    /**
//     * Получить стоимость всех заказов пользователя
//     *
//     * @param userUuid Идентификатор пользователя
//     * @return Стоимость всех заказов пользователя
//     */
//    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.userUuid = :userUuid")
//    Mono<BigDecimal> getTotalOrdersAmountByUser(@Param("userUuid") UUID userUuid);
}
