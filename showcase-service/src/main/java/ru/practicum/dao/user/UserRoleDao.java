package ru.practicum.dao.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * DAO роли пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("user_roles")
public class UserRoleDao {

    /**
     * Идентификатор пользователя
     */
    private UUID user_uuid;

    /**
     * Идентификатор роли
     */
    private UUID role_uuid;
}