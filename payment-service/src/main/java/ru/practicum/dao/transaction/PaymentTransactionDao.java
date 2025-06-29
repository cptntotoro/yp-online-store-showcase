package ru.practicum.dao.transaction;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import ru.practicum.model.transaction.TransactionStatus;
import ru.practicum.model.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DAO транзакции оплаты
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("payment_transactions")
public class PaymentTransactionDao {

    /**
     * Идентификатор
     */
    @Id
    @Column("transaction_uuid")
    private UUID transactionUuid;

    /**
     * Идентификатор пользователя
     */
    private UUID userUuid;

    /**
     * Идентификатор заказа
     */
    private UUID orderUuid;

    /**
     * Сумма
     */
    private BigDecimal amount;

    /**
     * Тип транзакции
     */
    @Column("transaction_type")
    private TransactionType transactionType;

    /**
     * Статус транзакции
     */
    @Column("transaction_status")
    private TransactionStatus status;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;
}
