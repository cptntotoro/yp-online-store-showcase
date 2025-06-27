package ru.practicum.mapper.transaction;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dao.transaction.PaymentTransactionDao;
import ru.practicum.model.transaction.PaymentTransaction;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentTransactionMapperTest {

    private final PaymentTransactionMapper paymentTransactionMapper = Mappers.getMapper(PaymentTransactionMapper.class);

    @Test
    void shouldMapPaymentTransactionToPaymentTransactionDao() {
        UUID transactionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setTransactionUuid(transactionId);
        transaction.setUserUuid(userId);
        transaction.setAmount(BigDecimal.valueOf(150.50));

        PaymentTransactionDao dao = paymentTransactionMapper.paymentTransactionToPaymentTransactionDao(transaction);

        assertThat(dao).isNotNull();
        assertThat(dao.getTransactionUuid()).isEqualTo(transactionId);
        assertThat(dao.getUserUuid()).isEqualTo(userId);
        assertThat(dao.getAmount()).isEqualTo(BigDecimal.valueOf(150.50));
    }

    @Test
    void shouldMapPaymentTransactionDaoToPaymentTransaction() {
        UUID transactionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        PaymentTransactionDao dao = new PaymentTransactionDao();
        dao.setTransactionUuid(transactionId);
        dao.setUserUuid(userId);
        dao.setAmount(BigDecimal.valueOf(250.75));

        PaymentTransaction transaction = paymentTransactionMapper.paymentTransactionDaoToPaymentTransaction(dao);

        assertThat(transaction).isNotNull();
        assertThat(transaction.getTransactionUuid()).isEqualTo(transactionId);
        assertThat(transaction.getUserUuid()).isEqualTo(userId);
        assertThat(transaction.getAmount()).isEqualTo(BigDecimal.valueOf(250.75));
    }

    @Test
    void shouldHandleNullPaymentTransaction() {
        PaymentTransactionDao dao = paymentTransactionMapper.paymentTransactionToPaymentTransactionDao(null);
        assertThat(dao).isNull();

        PaymentTransaction transaction = paymentTransactionMapper.paymentTransactionDaoToPaymentTransaction(null);
        assertThat(transaction).isNull();
    }
}