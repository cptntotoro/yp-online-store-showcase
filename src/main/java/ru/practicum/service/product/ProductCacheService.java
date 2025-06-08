package ru.practicum.service.product;

import reactor.core.publisher.Flux;
import ru.practicum.model.product.Product;

/**
 * Сервис кеширования товаров
 */
public interface ProductCacheService {
    /**
     * Получить все товары из кеша
     *
     * @return Все товары
     */
    Flux<Product> getAllProducts();
}
