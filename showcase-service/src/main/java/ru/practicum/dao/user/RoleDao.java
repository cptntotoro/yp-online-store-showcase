package ru.practicum.dao.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

/**
 * DAO пользовательской роли
 */
@Data
@Table("roles")
public class RoleDao {

    /**
     * Идентификатор
     */
    @Id
    @Column("role_uuid")
    private UUID uuid;

    /**
     * Название
     */
    private String name;

    /**
     * Описание
     */
    private String description;
}