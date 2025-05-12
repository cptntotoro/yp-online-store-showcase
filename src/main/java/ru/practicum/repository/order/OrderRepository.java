package ru.practicum.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.order.Order;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий заказов
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Получить список заказов по идентификатору пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Список заказов
     */
    List<Order> findByUserUuid(UUID userUuid);

    /**
     * Получить заказ пользователя по идентификаторам заказа и пользователя
     *
     * @param orderUuid Идентификатор заказа
     * @param userUuid Идентификатор пользователя
     * @return Optional с заказом, если найден
     */
    Optional<Order> findByUuidAndUserUuid(UUID orderUuid, UUID userUuid);

    /**
     * Получить заказ пользователя
     *
     * @param uuid Идентификатор заказа
     * @param userUuid Идентификатор пользователя
     * @return Optional с заказом, если найден
     */
    @Query("SELECT o FROM Order o WHERE o.uuid = :uuid AND o.userUuid = :userUuid")
    Optional<Order> findByIdWhereUserUuidIn(@Param("uuid") UUID uuid, @Param("userUuid") UUID userUuid);

    /**
     * Получить стоимость всех заказов пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Стоимость всех заказов пользователя
     */
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.userUuid = :userUuid")
    BigDecimal getTotalOrdersAmountByUser(@Param("userUuid") UUID userUuid);
}
