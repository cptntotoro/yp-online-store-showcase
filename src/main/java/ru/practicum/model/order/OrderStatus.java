package ru.practicum.model.order;

/**
 * Статус заказа
 */
public enum OrderStatus {
    CREATED,          // Заказ создан
    PROCESSING,       // В обработке
    PAYMENT_PENDING,  // Ожидает оплаты
    PAID,             // Оплачен
    SHIPPED,          // Отправлен
    DELIVERED,        // Доставлен
    CANCELLED,        // Отменен
    REFUNDED          // Возвращен
}