package ru.practicum.repository.order;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Order;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
