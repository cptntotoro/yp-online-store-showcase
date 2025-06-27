package ru.practicum.repository.user;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.dao.user.UserDao;

import java.util.UUID;

/**
 * Репозиторий пользователей
 */
@Repository
public interface UserRepository extends ReactiveCrudRepository<UserDao, UUID> {
}
