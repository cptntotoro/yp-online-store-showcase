package ru.practicum.model.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Пользователь
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Идентификатор
     */
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "user_uuid", updatable = false, nullable = false)
    private UUID uuid;

    /**
     * Имя пользователя
     */
    @Column(unique = true)
    private String username;

    /**
     * Адрес электронной почты
     */
    @Column(unique = true)
    private String email;

    /**
     * Дата создания
     */
    @CreationTimestamp
    private LocalDateTime createdAt;
}