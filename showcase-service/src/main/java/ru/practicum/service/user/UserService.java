package ru.practicum.service.user;

import reactor.core.publisher.Mono;
import ru.practicum.model.user.User;

import java.util.UUID;

/**
 * Сервис управления пользователями
 */
public interface UserService {
    /**
     * Добавить гостя
     *
     * @return Пользователь
     */
//    Mono<User> createGuest();

    /**
     * Зарегистрировать пользователя
     *
     * @param user Пользователь
     * @return Пользователь
     */
    Mono<User> register(User user);

    Mono<User> findByUsername(String username);

    /**
     * Проверить существование пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Да/Нет
     */
    Mono<Boolean> existsByUuid(UUID userUuid);

    Mono<Boolean> existsByUsername(String username);

    Mono<Boolean> existsByEmail(String email);
}
