package ru.practicum.dao.balance;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO баланса пользователя
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("user_balances")
public class UserBalanceDao {

    /**
     * Идентификатор
     */
    @Id
    @Column("balance_uuid")
    private UUID uuid;

    /**
     * Идентификатор пользователя
     */
    @Column("user_uuid")
    private UUID userUuid;

    /**
     * Баланс счета
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
