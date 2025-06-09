package ru.practicum.model.user;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Пользователь
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Идентификатор
     */
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
    private LocalDateTime createdAt;
}