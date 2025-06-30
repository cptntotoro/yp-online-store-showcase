package ru.practicum.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dto.product.ProductCacheDto;
import ru.practicum.exception.product.ProductNotFoundException;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.model.product.ProductSort;
import ru.practicum.repository.product.ProductRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    /**
     * Репозиторий товаров
     */
    private final ProductRepository productRepository;

    /**
     * Сервис кеширования товаров
     */
    private final ProductCacheService productCacheService;

    /**
     * Маппер товаров
     */
    private final ProductMapper productMapper;

    @Override
    public Mono<Page<Product>> getAll(Pageable pageable) {
        return getPagedProducts(flux -> flux, pageable);
    }

    @Override
    public Mono<Page<Product>> search(String query, Pageable pageable) {
        return getPagedProducts(flux -> flux.filter(product ->
                        product.getName().toLowerCase().contains(query.toLowerCase())),
                pageable);
    }

    @Override
    public Mono<Page<Product>> getSorted(String sort, Pageable pageable) {
        return getComparator(sort)
                .map(comparator -> getPagedProducts(flux -> flux.sort(comparator), pageable))
                .orElse(getPagedProducts(flux -> flux, pageable));
    }

    @Override
    public Mono<Page<Product>> getProducts(String search, String sort, Pageable pageable) {
        Flux<ProductCacheDto> productStream = productCacheService.getAllProducts();

        if (search != null && !search.isEmpty()) {
            String searchQuery = search.toLowerCase();
            productStream = productStream.filter(p -> p.getName().toLowerCase().contains(searchQuery));
        }

        Flux<ProductCacheDto> sortedStream = getComparator(sort)
                .map(productStream::sort)
                .orElse(productStream);

        return sortedStream
                .collectList()
                .flatMap(filteredProducts -> {
                    long totalCount = filteredProducts.size();
                    int totalPages = (int) Math.ceil((double) totalCount / pageable.getPageSize());
                    int pageNumber = Math.min(pageable.getPageNumber(), totalPages - 1);

                    Pageable correctedPageable = PageRequest.of(
                            Math.max(pageNumber, 0),
                            pageable.getPageSize(),
                            pageable.getSort()
                    );

                    List<Product> pagedProducts = filteredProducts.stream()
                            .skip(correctedPageable.getOffset())
                            .limit(correctedPageable.getPageSize())
                            .map(productMapper::fromCacheDto)
                            .collect(Collectors.toList());

                    return Mono.just(new PageImpl<>(pagedProducts, correctedPageable, totalCount));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Product> getByUuid(UUID uuid) {
        return productCacheService.getProductById(uuid)
                .map(productMapper::fromCacheDto)
                .switchIfEmpty(Mono.error(new ProductNotFoundException("Товар не найден")));
    }

    @Override
    @Transactional
    public Mono<Void> batchAdd(Flux<Product> products) {
        return products
                .collectList()
                .flatMap(productList -> productRepository.saveAll(
                                productList.stream()
                                        .map(productMapper::productToProductDao)
                                        .collect(Collectors.toList()))
                        .collectList()
                        .flatMap(savedProducts -> {
                            List<Product> productsToCache = savedProducts.stream()
                                    .map(productMapper::productDaoToProduct)
                                    .collect(Collectors.toList());
                            return productCacheService.cacheProducts(productsToCache)
                                    .then(productCacheService.evictListCache());
                        }));
    }

    @Override
    public Mono<Map<UUID, Product>> getProductsByIds(Set<UUID> productIds) {
        if (productIds.isEmpty()) {
            return Mono.just(Map.of());
        }

        return productCacheService.getAllProducts()
                .filter(dto -> productIds.contains(dto.getUuid()))
                .collectMap(ProductCacheDto::getUuid)
                .flatMap(cachedProducts -> {
                    Set<UUID> missingIds = productIds.stream()
                            .filter(id -> !cachedProducts.containsKey(id))
                            .collect(Collectors.toSet());

                    if (missingIds.isEmpty()) {
                        return Mono.just(cachedProducts.entrySet().stream()
                                .collect(Collectors.toMap(
                                        Map.Entry::getKey,
                                        e -> productMapper.fromCacheDto(e.getValue()))
                                ));
                    }

                    return productRepository.findAllById(missingIds)
                            .map(productMapper::productDaoToProduct)
                            .collectMap(Product::getUuid)
                            .map(dbProducts -> {
                                Map<UUID, Product> result = cachedProducts.entrySet().stream()
                                        .collect(Collectors.toMap(
                                                Map.Entry::getKey,
                                                e -> productMapper.fromCacheDto(e.getValue())));
                                result.putAll(dbProducts);
                                return result;
                            });
                });
    }

    private Mono<Page<Product>> getPagedProducts(
            Function<Flux<ProductCacheDto>, Flux<ProductCacheDto>> processor,
            Pageable pageable) {
        return processor.apply(productCacheService.getAllProducts())
                .collectList()
                .zipWith(productRepository.count())
                .map(tuple -> {
                    List<Product> products = tuple.getT1().stream()
                            .map(productMapper::fromCacheDto)
                            .collect(Collectors.toList());

                    return new PageImpl<>(
                            applyPagination(products, pageable),
                            pageable,
                            tuple.getT2()
                    );
                });
    }

    private Optional<Comparator<ProductCacheDto>> getComparator(String sort) {
        return ProductSort.fromString(sort)
                .map(ProductSort::getCacheDtoComparator);
    }

    private List<Product> applyPagination(List<Product> products, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), products.size());
        return start > end ? Collections.emptyList() : products.subList(start, end);
    }
}