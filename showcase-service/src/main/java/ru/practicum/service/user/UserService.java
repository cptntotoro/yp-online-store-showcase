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
    Mono<User> createGuest();

    /**
     * Зарегистрировать пользователя
     *
     * @param user Пользователь
     * @return Пользователь
     */
    Mono<User> register(User user);

    /**
     * Проверить существование пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Да/Нет
     */
    Mono<Boolean> existsByUuid(UUID userUuid);
}
