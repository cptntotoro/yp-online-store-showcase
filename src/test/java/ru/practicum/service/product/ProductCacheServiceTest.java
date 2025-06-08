package ru.practicum.service.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.practicum.dao.product.ProductDao;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.repository.product.ProductRepository;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCacheServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductCacheServiceImpl productCacheService;

    @Test
    void getAllProducts_whenProductsExist_shouldReturnProducts() {
        ProductDao productDao1 = new ProductDao();
        productDao1.setUuid(UUID.randomUUID());
        productDao1.setName("Product 1");

        ProductDao productDao2 = new ProductDao();
        productDao2.setUuid(UUID.randomUUID());
        productDao2.setName("Product 2");

        Product product1 = new Product();
        product1.setUuid(productDao1.getUuid());
        product1.setName(productDao1.getName());

        Product product2 = new Product();
        product2.setUuid(productDao2.getUuid());
        product2.setName(productDao2.getName());

        when(productRepository.findAll()).thenReturn(Flux.just(productDao1, productDao2));
        when(productMapper.productDaoToProduct(productDao1)).thenReturn(product1);
        when(productMapper.productDaoToProduct(productDao2)).thenReturn(product2);

        Flux<Product> result = productCacheService.getAllProducts();

        StepVerifier.create(result)
                .expectNextMatches(p ->
                        p.getUuid().equals(product1.getUuid()) &&
                                p.getName().equals(product1.getName()))
                .expectNextMatches(p ->
                        p.getUuid().equals(product2.getUuid()) &&
                                p.getName().equals(product2.getName()))
                .verifyComplete();

        verify(productRepository).findAll();
        verify(productMapper).productDaoToProduct(productDao1);
        verify(productMapper).productDaoToProduct(productDao2);
    }

    @Test
    void getAllProducts_whenNoProducts_shouldReturnEmptyFlux() {
        when(productRepository.findAll()).thenReturn(Flux.empty());

        Flux<Product> result = productCacheService.getAllProducts();

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();

        verify(productRepository).findAll();
    }

    @Test
    void evictAll_shouldDoNothing() {
        productCacheService.evictAll().subscribe();
    }
}