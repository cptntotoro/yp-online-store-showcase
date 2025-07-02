package ru.practicum.model.balance;

import lombok.*;

import java.math.BigDecimal;
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
     * Идентификатор пользователя
     */
    private UUID userUuid;

    /**
     * Баланс счета пользователя
     */
    private BigDecimal balance;
}