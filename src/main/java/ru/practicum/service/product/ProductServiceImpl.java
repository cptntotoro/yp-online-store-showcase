package ru.practicum.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.exception.product.ProductNotFoundException;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.model.product.ProductSort;
import ru.practicum.repository.product.ProductRepository;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

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
        return productCacheService.getAllProducts()
                .collectList()
                .zipWith(productRepository.count())
                .map(tuple -> toPage(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    public Mono<Page<Product>> search(String query, Pageable pageable) {
        return productCacheService.getAllProducts()
                .filter(product -> product.getName().toLowerCase().contains(query.toLowerCase()))
                .collectList()
                .zipWith(productRepository.count())
                .map(tuple -> toPage(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    public Mono<Page<Product>> getSorted(String sort, Pageable pageable) {
        Comparator<Product> comparator = ProductSort.fromString(sort)
                .map(ProductSort::getComparator)
                .orElse(Comparator.comparing(Product::getCreatedAt).reversed());

        return productCacheService.getAllProducts()
                .sort(comparator)
                .collectList()
                .zipWith(productRepository.count())
                .map(tuple -> toPage(tuple.getT1(), pageable, tuple.getT2()));
    }

    @Override
    public Mono<Product> getByUuid(UUID uuid) {
        return productCacheService.getAllProducts()
                .filter(product -> product.getUuid().equals(uuid))
                .next()
                .switchIfEmpty(Mono.error(new ProductNotFoundException("Товар не найден")));
    }

    @Override
    @CacheEvict(value = "allProducts", allEntries = true)
    public Mono<Void> batchAdd(Flux<Product> products) {
        return productRepository
                .saveAll(products.map(productMapper::productToProductDao))
                .then();
    }

    private Page<Product> toPage(List<Product> allItems, Pageable pageable, long total) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allItems.size());
        List<Product> pageItems = start > end ? List.of() : allItems.subList(start, end);
        return new PageImpl<>(pageItems, pageable, total);
    }
}
