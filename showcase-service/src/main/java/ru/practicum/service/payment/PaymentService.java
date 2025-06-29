//package ru.practicum.service.payment;
//
//import reactor.core.publisher.Mono;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
///**
// * Сервис оплаты заказа
// */
//public interface PaymentService {
//
//    /**
//     * Эмуляция процесса оплаты заказа
//     *
//     * @param userUuid   Идентификатор пользователя
//     * @param orderUuid  Идентификатор заказа
//     * @param cardNumber Номер карты
//     */
//    Mono<Void> checkout(UUID userUuid, UUID orderUuid, String cardNumber);
//
//    /**
//     * Проверить, достаточно ли баланса счета пользователя для оплаты заказа
//     *
//     * @param userId    Идентификатор пользователя
//     * @param orderUuid Идентификатор заказа
//     * @return Да / Нет
//     */
//    Mono<Boolean> isBalanceSufficient(UUID userId, UUID orderUuid);
//
//    /**
//     * Обработать возврат средств на баланс счета пользователя
//     *
//     * @param userUuid  Идентификатор пользователя
//     * @param total     Сумма возрата
//     * @param orderUuid Идентификатор заказа
//     */
//    Mono<Void> processRefund(UUID userUuid, BigDecimal total, UUID orderUuid);
//}
