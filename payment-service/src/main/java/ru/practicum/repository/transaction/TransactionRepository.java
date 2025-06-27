package ru.practicum.repository.transaction;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.dao.transaction.PaymentTransactionDao;

import java.util.UUID;

/**
 * Репозиторий транзакций
 */
@Repository
public interface TransactionRepository extends R2dbcRepository<PaymentTransactionDao, UUID> {
}