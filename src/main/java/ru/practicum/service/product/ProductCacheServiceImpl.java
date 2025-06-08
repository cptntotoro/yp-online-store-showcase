package ru.practicum.service.product;

import lombok.AllArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.repository.product.ProductRepository;

import java.util.List;
import java.util.Optional;

import static ru.practicum.config.CacheConfig.PRODUCT_CACHE_NAME;

@Service
@AllArgsConstructor
public class ProductCacheServiceImpl implements ProductCacheService {
    /**
     * Репозиторий товаров
     */
    private final ProductRepository productRepository;

    /**
     * Маппер товаров
     */
    private final ProductMapper productMapper;

    private final CacheManager cacheManager;

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Flux<Product> getAllProducts() {
        return Mono.fromCallable(this::getCachedProducts)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .onErrorResume(e -> fetchProductsFromRepository());
    }

    @Override
    public Mono<Void> evictAll() {
        return Mono.fromRunnable(this::clearCache)
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private List<Product> getCachedProducts() {
        return Optional.ofNullable(cacheManager.getCache(PRODUCT_CACHE_NAME))
                .map(cache -> cache.get("all", () -> fetchProductsFromRepository()
                        .collectList()
                        .block()))
                .orElseGet(() -> fetchProductsFromRepository()
                        .collectList()
                        .block());
    }

    private Flux<Product> fetchProductsFromRepository() {
        return productRepository.findAll()
                .map(productMapper::productDaoToProduct);
    }

    private void clearCache() {
        Optional.ofNullable(cacheManager.getCache(PRODUCT_CACHE_NAME))
                .ifPresent(Cache::clear);
    }
}
