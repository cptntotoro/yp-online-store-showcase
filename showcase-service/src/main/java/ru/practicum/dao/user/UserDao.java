package ru.practicum.dao.user;

import lombok.*;
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
     * Адрес электронной почты
     */
    private String email;

    /**
     * Дата создания
     */
    @Column("created_at")
    private LocalDateTime createdAt;
}