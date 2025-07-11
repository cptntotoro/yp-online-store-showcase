package ru.practicum.mapper.payment;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.PaymentResponseDto;
import ru.practicum.dto.RefundResponseDto;
import ru.practicum.model.balance.UserBalance;
import ru.practicum.model.payment.PaymentResult;
import ru.practicum.model.transaction.PaymentTransaction;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentMapperTest {

    private final PaymentMapper paymentMapper = Mappers.getMapper(PaymentMapper.class);

    @Test
    void shouldMapPaymentResultToPaymentResponse() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .userUuid(userId)
                .transactionUuid(transactionId)
                .build();

        UserBalance updatedBalance = new UserBalance();
        updatedBalance.setAmount(BigDecimal.valueOf(500.75));

        PaymentResult result = PaymentResult.builder()
                .transaction(transaction)
                .updatedBalance(updatedBalance)
                .build();

        PaymentResponseDto dto = paymentMapper.paymentResultToPaymentResponse(result);

        assertThat(dto).isNotNull();
        assertThat(dto.getUserUuid()).isEqualTo(userId);
        assertThat(dto.getTransactionUuid()).isEqualTo(transactionId);
        assertThat(dto.getNewBalance()).isEqualTo(BigDecimal.valueOf(500.75));
    }

    @Test
    void shouldMapPaymentResultToRefundResponse() {
        UUID userId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();

        PaymentTransaction transaction = PaymentTransaction.builder()
                .userUuid(userId)
                .transactionUuid(transactionId)
                .build();

        UserBalance updatedBalance = new UserBalance();
        updatedBalance.setAmount(BigDecimal.valueOf(300.25));

        PaymentResult result = PaymentResult.builder()
                .transaction(transaction)
                .updatedBalance(updatedBalance)
                .isSuccess(true)
                .message("Успешный возврат средств")
                .build();

        RefundResponseDto dto = paymentMapper.paymentResultToRefundResponse(result);

        assertThat(dto).isNotNull();
        assertThat(dto.getUserUuid()).isEqualTo(userId);
        assertThat(dto.getTransactionUuid()).isEqualTo(transactionId);
        assertThat(dto.getNewBalance()).isEqualTo(BigDecimal.valueOf(300.25));
        assertTrue(dto.getIsSuccess());
        assertThat(dto.getMessage()).isEqualTo("Успешный возврат средств");
    }

    @Test
    void shouldHandleNullPaymentResult() {
        PaymentResponseDto paymentDto = paymentMapper.paymentResultToPaymentResponse(null);
        assertThat(paymentDto).isNull();

        RefundResponseDto refundDto = paymentMapper.paymentResultToRefundResponse(null);
        assertThat(refundDto).isNull();
    }
}