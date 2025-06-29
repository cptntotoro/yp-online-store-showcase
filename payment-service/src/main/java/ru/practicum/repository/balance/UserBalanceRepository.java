package ru.practicum.repository.balance;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.practicum.dao.balance.UserBalanceDao;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Репозиторий баланса счета пользователя
 */
@Repository
public interface UserBalanceRepository extends ReactiveCrudRepository<UserBalanceDao, UUID> {

    /**
     * Создать баланс счета пользователя
     *
     * @param userUuid Идендтификатор пользователя
     * @param amount   Сумма
     * @return Баланс пользователя
     */
    @Query("INSERT INTO user_balances (user_uuid, amount) VALUES (:userUuid, :amount) RETURNING *")
    Mono<UserBalanceDao> create(UUID userUuid, BigDecimal amount);

    /**
     * Получить баланс пользователя по идентификатору
     *
     * @param userUuid Идендтификатор пользователя
     * @return Баланс пользователя
     */
    Mono<UserBalanceDao> findByUserUuid(UUID userUuid);

    /**
     * Снять средства с баланса пользователя
     *
     * @param userUuid Идендтификатор пользователя
     * @param amount   Сумма снятия
     * @return Баланс пользователя
     */
    @Query("UPDATE user_balances SET amount = amount - :amount WHERE user_uuid = :userUuid AND amount >= :amount RETURNING *")
    Mono<UserBalanceDao> deductBalance(UUID userUuid, BigDecimal amount);

    /**
     * Пополнить баланс пользователя
     *
     * @param userUuid Идендтификатор пользователя
     * @param amount   Сумма пополнения
     * @return Баланс пользователя
     */
    @Query("UPDATE user_balances SET amount = amount + :amount WHERE user_uuid = :userUuid RETURNING *")
    Mono<UserBalanceDao> addToBalance(UUID userUuid, BigDecimal amount);
}
