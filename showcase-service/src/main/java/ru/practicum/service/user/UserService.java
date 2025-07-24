package ru.practicum.service.user;

import reactor.core.publisher.Mono;
import ru.practicum.model.user.User;

import java.util.UUID;

/**
 * Сервис управления пользователями
 */
public interface UserService {

    /**
     * Зарегистрировать пользователя
     *
     * @param user Пользователь
     * @return Пользователь
     */
    Mono<User> register(User user);

    /**
     * Проверить существование пользователя по идентификатору
     *
     * @param userUuid Идентификатор пользователя
     * @return Да/Нет
     */
    Mono<Boolean> existsByUuid(UUID userUuid);

    /**
     * Проверить существование пользователя по имени пользователя (username)
     *
     * @param username Имя пользователя (username)
     * @return Да/Нет
     */
    Mono<Boolean> existsByUsername(String username);

    /**
     * Проверить существование пользователя по адресу электронной почты
     *
     * @param email Адрес электронной почты
     * @return Да/Нет
     */
    Mono<Boolean> existsByEmail(String email);
}
