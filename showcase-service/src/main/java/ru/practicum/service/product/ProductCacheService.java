package ru.practicum.service.product;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dto.product.ProductCacheDto;
import ru.practicum.model.product.Product;

import java.util.List;
import java.util.UUID;

/**
 * Сервис кеширования товаров
 */
public interface ProductCacheService {

    /**
     * Получить все товары из кеша
     *
     * @return Все товары
     */
    Flux<ProductCacheDto> getAllProducts();

    Mono<ProductCacheDto> getProductById(UUID id);

    /**
     * Удалить все товары
     */
    Mono<Void> evictAll();

    Mono<Void> cacheProducts(List<Product> products);

    Mono<Void> evictListCache();

    Mono<Void> evictProductCache(UUID productId);
}
