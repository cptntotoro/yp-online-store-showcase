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
     * Адрес электронной почты
     */
    private String email;

    /**
     * Дата создания
     */
    @Column("created_at")
    private LocalDateTime createdAt;

    /**
     * Флаг, указывающий, не заблокирована ли учетная запись
     */
    private boolean accountNonLocked;

    /**
     * Флаг, указывающий, не истек ли срок действия учетной записи
     */
    private boolean accountNonExpired;

    /**
     * Флаг, указывающий, не истек ли срок действия учетных данных (пароля)
     */
    private boolean credentialsNonExpired;

    /**
     * Флаг активности аккаунта
     */
    private boolean enabled;
}