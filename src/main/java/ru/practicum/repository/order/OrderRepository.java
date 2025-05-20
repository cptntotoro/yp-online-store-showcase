package ru.practicum.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.order.Order;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Получить список заказов по идентификатору пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Список заказов
     */
    List<Order> findByUserUuid(UUID userUuid);
}
