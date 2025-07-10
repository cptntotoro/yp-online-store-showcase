package ru.practicum.dao.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DAO пользователя
 */
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDao {

    /**
     * Идентификатор
     */
    @Id
    @Column("user_uuid")
    private UUID uuid;

    /**
     * Имя пользователя
     */
    private String username;

    /**
     * Пароль
     */
    private String password;

    /**
     * Роль
     */
    private List<String> roles;

    /**
     * Адрес электронной почты
     */
    private String email;

    /**
     * Дата создания
     */
    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("enabled")
    private boolean enabled = true;
}