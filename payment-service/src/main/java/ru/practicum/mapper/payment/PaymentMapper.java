package ru.practicum.mapper.payment;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.payment.PaymentResponseDto;
import ru.practicum.dto.refund.RefundResponseDto;
import ru.practicum.model.payment.PaymentResult;

/**
 * Маппер оплаты
 */
@Mapper(componentModel = "spring")
public interface PaymentMapper {

    /**
     * Смаппить результат оплаты в DTO ответа оплаты заказа
     *
     * @param result Результат оплаты
     * @return DTO ответа оплаты заказа
     */
    @Mapping(target = "userId", source = "transaction.userUuid")
    @Mapping(target = "transactionId", source = "transaction.transactionUuid")
    @Mapping(target = "newBalance", source = "updatedBalance.amount")
    // TODO: фигли оно не работает???
    @Mapping(target = "isSuccess", source = "success")
    PaymentResponseDto paymentResultToPaymentResponse(PaymentResult result);

    /**
     * Смаппить результат оплаты в DTO
     *
     * @param result Результат оплаты
     * @return DTO ответа на возврат средств
     */
    @Mapping(target = "userUuid", source = "transaction.userUuid")
    @Mapping(target = "isSuccess", source = "success")
    @Mapping(target = "transactionId", source = "transaction.transactionUuid")
    @Mapping(target = "newBalance", source = "updatedBalance.amount")
    RefundResponseDto paymentResultToRefundResponse(PaymentResult result);
}