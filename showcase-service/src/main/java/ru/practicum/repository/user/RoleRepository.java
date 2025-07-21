package ru.practicum.repository.user;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.practicum.dao.user.RoleDao;

import java.util.UUID;

/**
 * Репозиторий пользовательских ролей
 */
@Repository
public interface RoleRepository extends ReactiveCrudRepository<RoleDao, UUID> {

    /**
     * Найти роль по названию
     *
     * @param name Название роли
     * @return DAO пользовательской роли
     */
    @Query("SELECT * FROM roles WHERE name = :name")
    Mono<RoleDao> findByName(String name);
}