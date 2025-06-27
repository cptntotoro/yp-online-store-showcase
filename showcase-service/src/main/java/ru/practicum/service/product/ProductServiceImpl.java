package ru.practicum.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
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
        Comparator<Product> comparator = getComparator(sort);
        return getPagedProducts(flux -> flux.sort(comparator), pageable);
    }

    @Override
    public Mono<Page<Product>> getProducts(String search, String sort, Pageable pageable) {
        Comparator<Product> comparator = getComparator(sort);
        Flux<Product> productStream = productCacheService.getAllProducts();

        if (search != null && !search.isEmpty()) {
            String searchQuery = search.toLowerCase();
            productStream = productStream.filter(p -> p.getName().toLowerCase().contains(searchQuery));
        }

        return productStream
                .sort(comparator)
                .collectList()
                .map(filteredProducts -> {
                    long totalCount = filteredProducts.size();
                    int totalPages = (int) Math.ceil((double) totalCount / pageable.getPageSize());
                    int pageNumber = pageable.getPageNumber();

                    if (pageNumber >= totalPages && totalPages > 0) {
                        pageNumber = totalPages - 1;
                    }

                    Pageable correctedPageable = PageRequest.of(pageNumber, pageable.getPageSize());
                    List<Product> pagedProducts = applyPagination(filteredProducts, correctedPageable);

                    return new PageImpl<>(pagedProducts, correctedPageable, totalCount);
                });
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Mono<Product> getByUuid(UUID uuid) {
        return productCacheService.getAllProducts()
                .filter(product -> product.getUuid().equals(uuid))
                .next()
                .switchIfEmpty(Mono.error(new ProductNotFoundException("Товар не найден")));
    }

    @Override
    public Mono<Void> batchAdd(Flux<Product> products) {
        return productRepository
                .saveAll(products.map(productMapper::productToProductDao))
                .then()
                .doOnSuccess(unused -> productCacheService.evictAll().subscribe());
    }

    @Override
    public Mono<Map<UUID, Product>> getProductsByIds(Set<UUID> productIds) {
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
                                Map<UUID, Product> result = new HashMap<>(cachedProducts);
                                result.putAll(dbProducts);
                                return result;
                            });
                });
    }

    /**
     * Применить пагинацию к списку товаров
     *
     * @param products Список товаров
     * @param pageable Пагинация
     * @return Список товаров
     */
    private List<Product> applyPagination(List<Product> products, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), products.size());
        return start > end ? Collections.emptyList() : products.subList(start, end);
    }

    /**
     * Получить страницу товаров
     *
     * @param processor Список товаров
     * @param pageable  Пагинация
     * @return Страница товаров
     */
    private Mono<Page<Product>> getPagedProducts(
            Function<Flux<Product>, Flux<Product>> processor,
            Pageable pageable) {
        return processor.apply(productCacheService.getAllProducts())
                .collectList()
                .zipWith(productRepository.count())
                .map(tuple -> createPage(tuple.getT1(), pageable, tuple.getT2()));
    }

    /**
     * Получить компаратор товаров
     *
     * @param sort Сортировка
     * @return Компаратор товаров
     */
    private Comparator<Product> getComparator(String sort) {
        return ProductSort.fromString(sort)
                .map(ProductSort::getComparator)
                .orElse(Comparator.comparing(Product::getCreatedAt).reversed());
    }

    /**
     * Создать страницу товаров
     *
     * @param products Список товаров
     * @param pageable Пагинация
     * @param total    Количество товаров
     * @return Страница товаров
     */
    private Page<Product> createPage(List<Product> products, Pageable pageable, long total) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), products.size());
        return new PageImpl<>(
                start > end ? Collections.emptyList() : products.subList(start, end),
                pageable,
                total
        );
    }
}