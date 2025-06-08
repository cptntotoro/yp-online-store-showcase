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
import ru.practicum.exception.product.ProductNotFoundException;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.repository.product.ProductRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
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

    private final UUID productUuid = UUID.randomUUID();

    private final Product sampleProduct = Product.builder()
            .uuid(productUuid)
            .name("Test Product")
            .description("A sample product")
            .price(new BigDecimal("100.00"))
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(LocalDateTime.now())
            .imageUrl("http://image.url")
            .build();

    @Test
    void getAll_ShouldReturnPagedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> productList = List.of(sampleProduct);

        when(productCacheService.getAllProducts()).thenReturn(Flux.fromIterable(productList));
        when(productRepository.count()).thenReturn(Mono.just((long) productList.size()));

        StepVerifier.create(productService.getAll(pageable))
                .assertNext(page -> {
                    assertEquals(1, page.getContent().size());
                    assertEquals(sampleProduct.getName(), page.getContent().getFirst().getName());
                })
                .verifyComplete();
    }

    @Test
    void search_ShouldReturnMatchingProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> productList = List.of(sampleProduct);

        when(productCacheService.getAllProducts()).thenReturn(Flux.fromIterable(productList));
        when(productRepository.count()).thenReturn(Mono.just(1L));

        StepVerifier.create(productService.search("test", pageable))
                .assertNext(page -> {
                    assertEquals(1, page.getContent().size());
                    assertEquals("Test Product", page.getContent().getFirst().getName());
                })
                .verifyComplete();
    }

    @Test
    void getSorted_ShouldReturnProductsSortedByPriceDesc() {
        Pageable pageable = PageRequest.of(0, 10);

        Product cheaperProduct = Product.builder()
                .uuid(UUID.randomUUID())
                .name("Cheap Product")
                .price(new BigDecimal("10.00"))
                .createdAt(LocalDateTime.now())
                .build();

        List<Product> productList = List.of(sampleProduct, cheaperProduct);

        when(productCacheService.getAllProducts()).thenReturn(Flux.fromIterable(productList));
        when(productRepository.count()).thenReturn(Mono.just((long) productList.size()));

        StepVerifier.create(productService.getSorted("price-desc", pageable))
                .assertNext(page -> {
                    assertEquals(2, page.getContent().size());
                    assertEquals(sampleProduct.getPrice(), page.getContent().getFirst().getPrice());
                })
                .verifyComplete();
    }

    @Test
    void getByUuid_ShouldReturnProduct_WhenFound() {
        when(productCacheService.getAllProducts()).thenReturn(Flux.just(sampleProduct));

        StepVerifier.create(productService.getByUuid(productUuid))
                .expectNext(sampleProduct)
                .verifyComplete();
    }

    @Test
    void getByUuid_ShouldThrow_WhenNotFound() {
        when(productCacheService.getAllProducts()).thenReturn(Flux.empty());

        StepVerifier.create(productService.getByUuid(productUuid))
                .expectErrorMatches(ex -> ex instanceof ProductNotFoundException &&
                        ex.getMessage().contains("Товар не найден"))
                .verify();
    }

    @Test
    void batchAdd_ShouldMapAndSaveProducts() {
        ProductDao productDao = ProductDao.builder()
                .uuid(sampleProduct.getUuid())
                .name(sampleProduct.getName())
                .price(sampleProduct.getPrice())
                .build();

        when(productRepository.saveAll(any(Flux.class))).thenReturn(Flux.just(productDao));
        when(productCacheService.evictAll()).thenReturn(Mono.empty());

        StepVerifier.create(productService.batchAdd(Flux.just(sampleProduct)))
                .verifyComplete();

        verify(productRepository).saveAll(any(Flux.class));
        verify(productCacheService).evictAll();
    }

    @Test
    void getProductsByIds_ShouldReturnProductsFromCache() {
        UUID productId1 = UUID.randomUUID();
        UUID productId2 = UUID.randomUUID();
        Set<UUID> productIds = Set.of(productId1, productId2);

        Product product1 = Product.builder().uuid(productId1).name("Product 1").build();
        Product product2 = Product.builder().uuid(productId2).name("Product 2").build();

        when(productCacheService.getAllProducts())
                .thenReturn(Flux.just(product1, product2));

        Mono<Map<UUID, Product>> result = productService.getProductsByIds(productIds);

        StepVerifier.create(result)
                .assertNext(productsMap -> {
                    assertEquals(2, productsMap.size());
                    assertEquals("Product 1", productsMap.get(productId1).getName());
                    assertEquals("Product 2", productsMap.get(productId2).getName());
                })
                .verifyComplete();

        verify(productCacheService).getAllProducts();
    }

    @Test
    void getProductsByIds_ShouldFetchMissingProductsFromDb() {
        UUID cachedProductId = UUID.randomUUID();
        UUID missingProductId = UUID.randomUUID();
        Set<UUID> productIds = Set.of(cachedProductId, missingProductId);

        Product cachedProduct = Product.builder().uuid(cachedProductId).name("Cached Product").build();
        ProductDao missingProductDao = ProductDao.builder()
                .uuid(missingProductId)
                .name("Missing Product")
                .build();
        Product missingProduct = Product.builder()
                .uuid(missingProductId)
                .name("Missing Product")
                .build();

        when(productCacheService.getAllProducts())
                .thenReturn(Flux.just(cachedProduct));
        when(productRepository.findAllById(Set.of(missingProductId)))
                .thenReturn(Flux.just(missingProductDao));
        when(productMapper.productDaoToProduct(missingProductDao))
                .thenReturn(missingProduct);

        Mono<Map<UUID, Product>> result = productService.getProductsByIds(productIds);

        StepVerifier.create(result)
                .assertNext(productsMap -> {
                    assertEquals(2, productsMap.size());
                    assertEquals("Cached Product", productsMap.get(cachedProductId).getName());
                    assertEquals("Missing Product", productsMap.get(missingProductId).getName());
                })
                .verifyComplete();

        verify(productCacheService).getAllProducts();
        verify(productRepository).findAllById(Set.of(missingProductId));
        verify(productMapper).productDaoToProduct(missingProductDao);
    }

    @Test
    void getProductsByIds_ShouldReturnEmptyMapForEmptyInput() {
        Mono<Map<UUID, Product>> result = productService.getProductsByIds(Collections.emptySet());

        StepVerifier.create(result)
                .assertNext(productsMap -> assertTrue(productsMap.isEmpty()))
                .verifyComplete();

        verify(productCacheService, never()).getAllProducts();
    }

    @Test
    void getProducts_ShouldReturnAllProductsWhenNoFilters() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime now = LocalDateTime.now();

        Product product1 = Product.builder()
                .uuid(UUID.randomUUID())
                .name("Product 1")
                .price(BigDecimal.valueOf(100))
                .createdAt(now.minusDays(1))
                .build();

        Product product2 = Product.builder()
                .uuid(UUID.randomUUID())
                .name("Product 2")
                .price(BigDecimal.valueOf(200))
                .createdAt(now)
            .build();

        when(productCacheService.getAllProducts()).thenReturn(Flux.just(product1, product2));

        StepVerifier.create(productService.getProducts(null, null, pageable))
                .assertNext(page -> {
                    assertEquals(2, page.getTotalElements());
                    assertEquals(1, page.getTotalPages());
                    assertEquals(2, page.getContent().size());
                })
                .verifyComplete();
    }

    @Test
    void getProducts_ShouldFilterBySearchQuery() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime now = LocalDateTime.now();

        Product matchingProduct = Product.builder()
                .uuid(UUID.randomUUID())
                .name("Apple iPhone")
                .price(BigDecimal.valueOf(1000))
                .createdAt(now)
            .build();

        Product nonMatchingProduct = Product.builder()
                .uuid(UUID.randomUUID())
                .name("Samsung Galaxy")
                .price(BigDecimal.valueOf(900))
                .createdAt(now.minusDays(1))
            .build();

        when(productCacheService.getAllProducts()).thenReturn(Flux.just(matchingProduct, nonMatchingProduct));

        StepVerifier.create(productService.getProducts("iphone", null, pageable))
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements());
                    assertEquals("Apple iPhone", page.getContent().getFirst().getName());
                })
                .verifyComplete();
    }

    @Test
    void getProducts_ShouldSortByPriceAsc() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime now = LocalDateTime.now();

        Product cheapProduct = Product.builder()
                .uuid(UUID.randomUUID())
                .name("Cheap")
                .price(BigDecimal.valueOf(100))
                .createdAt(now)
            .build();

        Product expensiveProduct = Product.builder()
                .uuid(UUID.randomUUID())
                .name("Expensive")
                .price(BigDecimal.valueOf(500))
                .createdAt(now.minusDays(1))
            .build();

        when(productCacheService.getAllProducts()).thenReturn(Flux.just(expensiveProduct, cheapProduct));

        StepVerifier.create(productService.getProducts(null, "price-asc", pageable))
                .assertNext(page -> {
                    assertEquals(2, page.getContent().size());
                    assertEquals(cheapProduct.getPrice(), page.getContent().getFirst().getPrice());
                    assertEquals(expensiveProduct.getPrice(), page.getContent().getLast().getPrice());
                })
                .verifyComplete();
    }

    @Test
    void getProducts_ShouldApplyPagination() {
        Pageable pageable = PageRequest.of(1, 1);
        LocalDateTime now = LocalDateTime.now();

        Product product1 = Product.builder()
                .uuid(UUID.randomUUID())
                .name("Product 1")
                .price(BigDecimal.valueOf(100))
                .createdAt(now)
                .build();

        Product product2 = Product.builder()
                .uuid(UUID.randomUUID())
                .name("Product 2")
                .price(BigDecimal.valueOf(200))
                .createdAt(now.minusDays(1))
                .build();

        when(productCacheService.getAllProducts()).thenReturn(Flux.just(product1, product2));

        StepVerifier.create(productService.getProducts(null, null, pageable))
                .assertNext(page -> {
                    assertEquals(2, page.getTotalElements());
                    assertEquals(2, page.getTotalPages());
                    assertEquals(1, page.getContent().size());
                    assertEquals(product2.getName(), page.getContent().getFirst().getName());
                })
                .verifyComplete();
    }

    @Test
    void getProducts_ShouldHandleEmptyResult() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productCacheService.getAllProducts()).thenReturn(Flux.empty());

        StepVerifier.create(productService.getProducts(null, null, pageable))
                .assertNext(page -> {
                    assertEquals(0, page.getTotalElements());
                    assertTrue(page.getContent().isEmpty());
                })
                .verifyComplete();
    }

    @Test
    void getProducts_ShouldCorrectPageNumberWhenExceedsTotalPages() {
        Pageable pageable = PageRequest.of(2, 10);
        LocalDateTime now = LocalDateTime.now();

        Product product = Product.builder()
                .uuid(UUID.randomUUID())
                .name("Single Product")
                .price(BigDecimal.valueOf(100))
                .createdAt(now)
                .build();

        when(productCacheService.getAllProducts()).thenReturn(Flux.just(product));

        StepVerifier.create(productService.getProducts(null, null, pageable))
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements());
                    assertEquals(1, page.getTotalPages());
                    assertEquals(0, page.getNumber());
                    assertEquals(1, page.getContent().size());
                })
                .verifyComplete();
    }
}
