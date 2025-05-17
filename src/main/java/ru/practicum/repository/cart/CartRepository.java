package ru.practicum.repository.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.cart.Cart;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUserUuid(UUID userUuid);
}
