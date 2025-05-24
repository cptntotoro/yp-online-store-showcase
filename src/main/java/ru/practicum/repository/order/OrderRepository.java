package ru.practicum.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.model.order.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Получить список заказов по идентификатору пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Список заказов
     */
    List<Order> findByUserUuid(UUID userUuid);

    @Query("SELECT o FROM Order o WHERE o.uuid = :uuid AND o.userUuid = :userUuid")
    Optional<Order> findByIdWhereUserUuidIn(@Param("uuid") UUID uuid,
                                            @Param("userUuid") UUID userUuid);
}
