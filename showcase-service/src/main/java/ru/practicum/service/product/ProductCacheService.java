package ru.practicum.service.product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.product.Product;

import java.util.List;
import java.util.UUID;

/**
 * Сервис кеширования товаров
 */
public interface ProductCacheService {

    /**
     * Получить все товары
     *
     * @return Все товары
     */
    Flux<Product> getAllProducts();

    /**
     * Получить товар по идентификатору
     *
     * @param uuid Идентификатор товара
     * @return Товар
     */
    Mono<Product> getProductById(UUID uuid);

    /**
     * Добавить товары
     *
     * @param products Список товаров
     */
    Mono<Void> cacheProducts(List<Product> products);

    /**
     * Очистить кеш списка товаров
     */
    Mono<Void> evictListCache();
}
