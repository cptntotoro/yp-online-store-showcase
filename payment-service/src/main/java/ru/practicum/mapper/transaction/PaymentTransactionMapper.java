package ru.practicum.mapper.transaction;

import org.mapstruct.Mapper;
import ru.practicum.dao.transaction.PaymentTransactionDao;
import ru.practicum.model.transaction.PaymentTransaction;

/**
 * Маппер транзакции оплаты
 */
@Mapper(componentModel = "spring")
public interface PaymentTransactionMapper {

    /**
     * Смаппить платежную транзакцию в DAO транзакции оплаты
     *
     * @param paymentTransaction Транзакция оплаты
     * @return DAO транзакции оплаты
     */
    PaymentTransactionDao paymentTransactionToPaymentTransactionDao(PaymentTransaction paymentTransaction);

    /**
     * Смаппить DAO транзакции оплаты в транзакцию оплаты
     *
     * @param paymentTransactionDao DAO транзакции оплаты
     * @return Транзакция оплаты
     */
    PaymentTransaction paymentTransactionDaoToPaymentTransaction(PaymentTransactionDao paymentTransactionDao);
}