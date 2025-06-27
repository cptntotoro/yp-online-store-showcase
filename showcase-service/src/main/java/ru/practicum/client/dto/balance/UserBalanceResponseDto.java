package ru.practicum.client.dto.balance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO ответа баланса пользователя
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBalanceResponseDto {

    /**
     * Идентификатор пользователя
     */
    private UUID userUuid;

    /**
     * Баланс счета
     */
    private BigDecimal balance;
}