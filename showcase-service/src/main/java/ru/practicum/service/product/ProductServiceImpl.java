package ru.practicum.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
        Flux<Product> productStream = productCacheService.getAllProducts();

        if (search != null && !search.isEmpty()) {
            String searchQuery = search.toLowerCase();
            productStream = productStream.filter(p -> p.getName().toLowerCase().contains(searchQuery));
        }

        Flux<Product> sortedStream = getComparator(sort)
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
                            .collect(Collectors.toList());

                    return Mono.just(new PageImpl<>(pagedProducts, correctedPageable, totalCount));
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<Product> getByUuid(UUID uuid) {
        return productCacheService.getProductById(uuid)
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
    public Mono<Map<UUID, Product>> getProductsByUuids(Set<UUID> productIds) {
        if (productIds.isEmpty()) {
            return Mono.just(Map.of());
        }

        return productCacheService.getAllProducts()
                .filter(product -> productIds.contains(product.getUuid()))
                .collectMap(Product::getUuid)
                .flatMap(cachedProducts -> {
                    Set<UUID> missingIds = productIds.stream()
                            .filter(id -> !cachedProducts.containsKey(id))
                            .collect(Collectors.toSet());

                    if (missingIds.isEmpty()) {
                        return Mono.just(cachedProducts);
                    }

                    return productRepository.findAllById(missingIds)
                            .map(productMapper::productDaoToProduct)
                            .collectMap(Product::getUuid)
                            .map(dbProducts -> {
                                cachedProducts.putAll(dbProducts);
                                return cachedProducts;
                            });
                });
    }

    private Mono<Page<Product>> getPagedProducts(
            Function<Flux<Product>, Flux<Product>> processor,
            Pageable pageable) {
        return processor.apply(productCacheService.getAllProducts())
                .collectList()
                .zipWith(productRepository.count())
                .map(tuple -> new PageImpl<>(
                        applyPagination(tuple.getT1(), pageable),
                        pageable,
                        tuple.getT2()
                ));
    }

    private Optional<Comparator<Product>> getComparator(String sort) {
        return ProductSort.fromString(sort)
                .map(ProductSort::getComparator);
    }

    private List<Product> applyPagination(List<Product> products, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), products.size());
        return start > end ? Collections.emptyList() : products.subList(start, end);
    }
}