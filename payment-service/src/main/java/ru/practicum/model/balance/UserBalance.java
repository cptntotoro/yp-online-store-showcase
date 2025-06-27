package ru.practicum.model.balance;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Баланс счета пользователя
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBalance {

    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Идентификатор пользователя
     */
    private UUID userUuid;

    /**
     * Сумма баланса
     */
    private BigDecimal amount;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;

    /**
     * Дата обновления
     */
    private LocalDateTime updatedAt;
}
