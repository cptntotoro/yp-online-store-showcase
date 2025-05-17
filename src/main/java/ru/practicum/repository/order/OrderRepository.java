package ru.practicum.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.order.Order;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    List<Order> findByUserUuid(UUID userUuid);
}
