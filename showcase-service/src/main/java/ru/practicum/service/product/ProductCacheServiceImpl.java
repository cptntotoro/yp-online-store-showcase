package ru.practicum.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.practicum.dto.product.cache.ProductCacheDto;
import ru.practicum.exception.product.ProductNotFoundException;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.repository.product.ProductRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCacheServiceImpl implements ProductCacheService {

    /**
     * Репозиторий товаров
     */
    private final ProductRepository productRepository;

    /**
     * Маппер товаров
     */
    private final ProductMapper productMapper;

    /**
     * Кеш товаров
     */
    private final ReactiveRedisTemplate<String, ProductCacheDto> productCacheTemplate;

    /**
     * Кеш всех товаров
     */
    private final ReactiveRedisTemplate<String, List<ProductCacheDto>> listCacheTemplate;

    private static final String ALL_PRODUCTS_KEY = "all_products";
    private static final String PRODUCT_KEY_PREFIX = "product:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(2);

    @Override
    public Flux<Product> getAllProducts() {
        return listCacheTemplate.opsForValue().get(ALL_PRODUCTS_KEY)
                .defaultIfEmpty(Collections.emptyList())
                .flatMapMany(list -> list.isEmpty()
                        ? fetchAndCacheAllProducts()
                        : Flux.fromIterable(list).map(productMapper::productCacheDtoToProduct));
    }

    @Override
    public Mono<Product> getProductById(UUID uuid) {
        return productCacheTemplate.opsForValue().get(PRODUCT_KEY_PREFIX + uuid)
                .flatMap(dto -> Mono.justOrEmpty(productMapper.productCacheDtoToProduct(dto)))
                .switchIfEmpty(fetchAndCacheProduct(uuid))
                .onErrorResume(e -> fetchAndCacheProduct(uuid));
    }

    @Override
    public Mono<Void> cacheProducts(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return Mono.empty();
        }

        List<ProductCacheDto> dtos = products.stream()
                .map(productMapper::productToCacheDto)
                .collect(Collectors.toList());

        Mono<Void> cacheIndividualProducts = Flux.fromIterable(dtos)
                .parallel()
                .runOn(Schedulers.boundedElastic())
                .flatMap(dto -> productCacheTemplate.opsForValue()
                        .set(PRODUCT_KEY_PREFIX + dto.getUuid(), dto, CACHE_TTL))
                .sequential()
                .then();

        Mono<Void> updateProductList = listCacheTemplate.opsForValue()
                .set(ALL_PRODUCTS_KEY, dtos, CACHE_TTL)
                .then();

        return Mono.when(cacheIndividualProducts, updateProductList);
    }

    @Override
    public Mono<Void> evictListCache() {
        return listCacheTemplate.delete(ALL_PRODUCTS_KEY).then();
    }

    private Flux<Product> fetchAndCacheAllProducts() {
        return productRepository.findAll()
                .map(productMapper::productDaoToProduct)
                .collectList()
                .flatMapMany(products -> {
                    List<ProductCacheDto> dtos = products.stream()
                            .map(productMapper::productToCacheDto)
                            .collect(Collectors.toList());

                    return listCacheTemplate.opsForValue()
                            .set(ALL_PRODUCTS_KEY, dtos, CACHE_TTL)
                            .thenMany(Flux.fromIterable(products));
                });
    }

    private Mono<Product> fetchAndCacheProduct(UUID id) {
        return Mono.defer(() -> productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException("Product not found")))
                .flatMap(dao -> {
                    Product product = productMapper.productDaoToProduct(dao);
                    ProductCacheDto dto = productMapper.productToCacheDto(product);

                    return Mono.zip(
                            productCacheTemplate.opsForValue()
                                    .set(PRODUCT_KEY_PREFIX + id, dto, CACHE_TTL)
                                    .onErrorResume(e -> Mono.empty()),
                            listCacheTemplate.opsForValue().get(ALL_PRODUCTS_KEY)
                                    .defaultIfEmpty(new ArrayList<>())
                                    .flatMap(list -> {
                                        list.removeIf(item -> item.getUuid().equals(id));
                                        list.add(dto);
                                        return listCacheTemplate.opsForValue()
                                                .set(ALL_PRODUCTS_KEY, list, CACHE_TTL)
                                                .onErrorResume(e -> Mono.empty());
                                    })
                    ).thenReturn(product);
                }));
    }
}