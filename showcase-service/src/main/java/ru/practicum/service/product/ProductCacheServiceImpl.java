package ru.practicum.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.practicum.dto.product.ProductCacheDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.repository.product.ProductRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCacheServiceImpl implements ProductCacheService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final ReactiveRedisTemplate<String, ProductCacheDto> productCacheTemplate;

    private final ReactiveRedisTemplate<String, List<ProductCacheDto>> listCacheTemplate;

    private static final String ALL_PRODUCTS_KEY = "all_products";
    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(2);

    @Override
    public Flux<ProductCacheDto> getAllProducts() {
        return listCacheTemplate.opsForValue().get(ALL_PRODUCTS_KEY)
                .flatMapMany(Flux::fromIterable)
                .switchIfEmpty(fetchAndCacheAllProducts());
    }

    @Override
    public Mono<ProductCacheDto> getProductById(UUID id) {
        return productCacheTemplate.opsForValue().get(PRODUCT_KEY_PREFIX + id)
                .switchIfEmpty(fetchAndCacheProduct(id));
    }

    @Override
    public Mono<Void> cacheProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Mono.empty();
        }

        List<ProductCacheDto> dtos = products.stream()
                .map(productMapper::toCacheDto)
                .collect(Collectors.toList());

        // Кэшируем каждый продукт индивидуально
        Mono<Void> cacheIndividualProducts = Flux.fromIterable(dtos)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(dto -> productCacheTemplate.opsForValue()
                        .set(PRODUCT_KEY_PREFIX + dto.getUuid(), dto, CACHE_TTL))
                .sequential()
                .then();

        // Обновляем список всех продуктов
        Mono<Void> updateProductList = listCacheTemplate.opsForValue()
                .set(ALL_PRODUCTS_KEY, dtos, CACHE_TTL)
                .then();

        return Mono.when(cacheIndividualProducts, updateProductList);
    }

    @Override
    public Mono<Void> evictAll() {
        return listCacheTemplate.delete(ALL_PRODUCTS_KEY)
                .then(productCacheTemplate.keys(PRODUCT_KEY_PREFIX + "*")
                        .flatMap(productCacheTemplate::delete)
                        .then()
                );
    }

    @Override
    public Mono<Void> evictListCache() {
        return listCacheTemplate.delete(ALL_PRODUCTS_KEY).then();
    }

    @Override
    public Mono<Void> evictProductCache(UUID productId) {
        return productCacheTemplate.delete(PRODUCT_KEY_PREFIX + productId)
                .then(listCacheTemplate.opsForValue().get(ALL_PRODUCTS_KEY)
                        .defaultIfEmpty(new ArrayList<>())
                        .flatMap(list -> {
                            list.removeIf(dto -> dto.getUuid().equals(productId));
                            return listCacheTemplate.opsForValue()
                                    .set(ALL_PRODUCTS_KEY, list, CACHE_TTL);
                        })
                )
                .then();
    }

    private Flux<ProductCacheDto> fetchAndCacheAllProducts() {
        return productRepository.findAll()
                .map(productMapper::toCacheDto)
                .collectList()
                .flatMapMany(dtos -> listCacheTemplate.opsForValue()
                        .set(ALL_PRODUCTS_KEY, dtos, CACHE_TTL)
                        .thenMany(Flux.fromIterable(dtos))
                );
    }

    private Mono<ProductCacheDto> fetchAndCacheProduct(UUID id) {
        return productRepository.findById(id)
                .map(productMapper::toCacheDto)
                .flatMap(dto -> Mono.zip(
                        productCacheTemplate.opsForValue()
                                .set(PRODUCT_KEY_PREFIX + id, dto, CACHE_TTL),
                        listCacheTemplate.opsForValue().get(ALL_PRODUCTS_KEY)
                                .defaultIfEmpty(new ArrayList<>())
                                .flatMap(list -> {
                                    list.removeIf(item -> item.getUuid().equals(id));
                                    list.add(dto);
                                    return listCacheTemplate.opsForValue()
                                            .set(ALL_PRODUCTS_KEY, list, CACHE_TTL);
                                })
                ).thenReturn(dto));
    }
}