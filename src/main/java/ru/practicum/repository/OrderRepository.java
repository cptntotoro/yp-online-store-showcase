package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
