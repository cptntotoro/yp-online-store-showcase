package ru.practicum.service.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.product.ProductDao;
import ru.practicum.dto.product.ProductCacheDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.repository.product.ProductRepository;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductCacheService productCacheService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private final UUID productId = UUID.randomUUID();
    private final Product testProduct = Product.builder()
            .uuid(productId)
            .name("Test Product")
            .price(BigDecimal.valueOf(100))
            .build();

    private final ProductCacheDto testProductCacheDto = ProductCacheDto.builder()
            .uuid(productId)
            .name("Test Product")
            .price(BigDecimal.valueOf(100))
            .build();

    @Test
    void getProducts_ShouldCombineSearchAndSort() {
        Pageable pageable = PageRequest.of(0, 10);
        ProductCacheDto matchingProduct1 = ProductCacheDto.builder()
                .uuid(UUID.randomUUID())
                .name("Apple iPhone 13")
                .price(BigDecimal.valueOf(999))
                .build();
        ProductCacheDto matchingProduct2 = ProductCacheDto.builder()
                .uuid(UUID.randomUUID())
                .name("Apple iPhone 12")
                .price(BigDecimal.valueOf(799))
                .build();
        ProductCacheDto nonMatchingProduct = ProductCacheDto.builder()
                .uuid(UUID.randomUUID())
                .name("Samsung Galaxy")
                .price(BigDecimal.valueOf(899))
                .build();

        when(productCacheService.getAllProducts()).thenReturn(Flux.just(matchingProduct1, nonMatchingProduct, matchingProduct2));
        when(productMapper.fromCacheDto(matchingProduct2)).thenReturn(Product.builder()
                .uuid(matchingProduct2.getUuid())
                .name(matchingProduct2.getName())
                .price(matchingProduct2.getPrice())
                .build());
        when(productMapper.fromCacheDto(matchingProduct1)).thenReturn(Product.builder()
                .uuid(matchingProduct1.getUuid())
                .name(matchingProduct1.getName())
                .price(matchingProduct1.getPrice())
                .build());

        StepVerifier.create(productService.getProducts("iphone", "price-asc", pageable))
                .assertNext(page -> {
                    assertEquals(2, page.getContent().size());
                    assertEquals("Apple iPhone 12", page.getContent().getFirst().getName());
                    assertEquals("Apple iPhone 13", page.getContent().getLast().getName());
                })
                .verifyComplete();
    }

    @Test
    void batchAdd_ShouldHandleEmptyInput() {
        when(productRepository.saveAll(any(Flux.class))).thenReturn(Flux.empty());

        StepVerifier.create(productService.batchAdd(Flux.empty()))
                .verifyComplete();

        verify(productCacheService, never()).cacheProducts(any());
        verify(productCacheService, never()).evictListCache();
    }

    @Test
    void batchAdd_ShouldSaveAndCacheProducts() {
        Product product = Product.builder().uuid(UUID.randomUUID()).name("Test").build();
        ProductDao productDao = ProductDao.builder().uuid(product.getUuid()).name("Test").build();

        when(productRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(productDao));
        when(productMapper.productToProductDao(product)).thenReturn(productDao);
        when(productMapper.productDaoToProduct(productDao)).thenReturn(product);
        when(productCacheService.cacheProducts(List.of(product))).thenReturn(Mono.empty());
        when(productCacheService.evictListCache()).thenReturn(Mono.empty());

        StepVerifier.create(productService.batchAdd(Flux.just(product)))
                .verifyComplete();

        verify(productCacheService).cacheProducts(List.of(product));
        verify(productCacheService).evictListCache();
    }

    @Test
    void batchAdd_ShouldPropagateErrorFromRepository() {
        Product product = Product.builder().uuid(UUID.randomUUID()).name("Test").build();

        when(productRepository.saveAll(any(Flux.class))).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(productService.batchAdd(Flux.just(product)))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("DB error"))
                .verify();

        verify(productCacheService, never()).cacheProducts(any());
    }

    @Test
    void getProductsByIds_ShouldHandlePartialCacheMiss() {
        UUID cachedId = UUID.randomUUID();
        UUID missingId = UUID.randomUUID();
        ProductDao missingProductDao = ProductDao.builder()
                .uuid(missingId)
                .name("Missing Product")
                .build();
        Product missingProduct = Product.builder()
                .uuid(missingId)
                .name("Missing Product")
                .build();

        when(productCacheService.getAllProducts()).thenReturn(Flux.just(
                ProductCacheDto.builder().uuid(cachedId).name("Cached Product").build()
        ));
        when(productRepository.findAllById(Set.of(missingId))).thenReturn(Flux.just(missingProductDao));
        when(productMapper.productDaoToProduct(missingProductDao)).thenReturn(missingProduct);
        when(productMapper.fromCacheDto(any())).thenAnswer(inv ->
                Product.builder()
                        .uuid(((ProductCacheDto) inv.getArgument(0)).getUuid())
                        .name(((ProductCacheDto) inv.getArgument(0)).getName())
                        .build());

        StepVerifier.create(productService.getProductsByIds(Set.of(cachedId, missingId)))
                .assertNext(map -> {
                    assertEquals(2, map.size());
                    assertTrue(map.containsKey(cachedId));
                    assertTrue(map.containsKey(missingId));
                })
                .verifyComplete();
    }

    @Test
    void getSorted_ShouldHandleEmptySortParameter() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productCacheService.getAllProducts()).thenReturn(Flux.just(testProductCacheDto));
        when(productRepository.count()).thenReturn(Mono.just(1L));
        when(productMapper.fromCacheDto(testProductCacheDto)).thenReturn(testProduct);

        StepVerifier.create(productService.getSorted(null, pageable))
                .assertNext(page -> {
                    assertEquals(1, page.getContent().size());
                    assertEquals(testProduct.getName(), page.getContent().getFirst().getName());
                })
                .verifyComplete();
    }

    @Test
    void getProducts_ShouldHandleNullSearchAndSort() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productCacheService.getAllProducts()).thenReturn(Flux.just(testProductCacheDto));
        when(productMapper.fromCacheDto(testProductCacheDto)).thenReturn(testProduct);

        StepVerifier.create(productService.getProducts(null, null, pageable))
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements());
                    assertEquals(testProduct.getName(), page.getContent().getFirst().getName());
                })
                .verifyComplete();
    }
}