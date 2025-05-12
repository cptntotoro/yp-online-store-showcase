package ru.practicum.service.user;

import ru.practicum.model.user.User;

import java.util.UUID;

/**
 * Сервис управления пользователями
 */
public interface UserService {
    /**
     * Добавить нового пользователя
     *
     * @return Пользователь
     */
    User add();

    /**
     * Проверить существование пользователя
     *
     * @param userUuid Идентификатор пользователя
     * @return Да/Нет
     */
    boolean existsByUuid(UUID userUuid);
}
