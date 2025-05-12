package ru.practicum.repository.cart;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.cart.Cart;

import java.util.Optional;
import java.util.UUID;

/**
 * Репозиторий корзины товаров
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

    /**
     * Получить корзину по идентификатору пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Optional с корзиной, если найдена
     */
    Optional<Cart> findByUserUuid(UUID userUuid);
}
