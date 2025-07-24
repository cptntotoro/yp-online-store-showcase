package ru.practicum.repository.user;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.user.UserRoleDao;

import java.util.UUID;

/**
 * Репозиторий ролей пользователей
 */
@Repository
public interface UserRoleRepository extends ReactiveCrudRepository<UserRoleDao, UUID> {

    /**
     * Получить роли пользователя по его идентификатору
     *
     * @param userUuid Идентификатор пользователя
     * @return Список ролей
     */
    @Query("SELECT r.name FROM roles r JOIN user_roles ur ON r.role_uuid = ur.role_uuid WHERE ur.user_uuid = :userUuid")
    Flux<String> findRolesByUserUuid(UUID userUuid);

    /**
     * Добавить роль пользователю
     *
     * @param userUuid Идентификатор пользователя
     * @param roleUuid Идентификатор роли
     */
    @Query("INSERT INTO user_roles (user_uuid, role_uuid) VALUES (:userUuid, :roleUuid) RETURNING *")
    Mono<UserRoleDao> addRoleToUser(UUID userUuid, UUID roleUuid);
}