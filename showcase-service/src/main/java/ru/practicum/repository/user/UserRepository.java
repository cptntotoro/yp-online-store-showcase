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
}
