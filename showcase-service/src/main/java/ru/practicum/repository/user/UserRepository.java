package ru.practicum.repository.user;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.practicum.dao.user.UserDao;

import java.util.UUID;

/**
 * Репозиторий пользователей
 */
@Repository
public interface UserRepository extends ReactiveCrudRepository<UserDao, UUID> {
    /**
     * Получить пользователя по имени пользователя
     *
     * @param username Имя пользователя
     * @return Пользователь
     */
    Mono<UserDao> findByUsername(String username);

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
