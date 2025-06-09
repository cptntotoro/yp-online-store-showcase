package ru.practicum.service.product;

import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.repository.product.ProductRepository;

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

    /**
     * Кешируемая загрузка всех товаров
     */
    @Cacheable("products")
    @Override
    public Flux<Product> getAllProducts() {
        return productRepository.findAll().map(productMapper::productDaoToProduct).cache();
    }

    /**
     * Очистка кеша товаров
     */
    @CacheEvict(value = "products", allEntries = true)
    public void evictAll() {
    }
}
