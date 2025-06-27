package ru.practicum.service.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
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
import java.util.*;

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

    private Product testProduct;
    private UUID testUuid;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testUuid = UUID.randomUUID();
        testProduct = new Product();
        testProduct.setUuid(testUuid);
        testProduct.setName("Test Product");
        testProduct.setPrice(BigDecimal.valueOf(100.0));

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAll_shouldReturnPageOfProducts() {
        when(productCacheService.getAllProducts()).thenReturn(Flux.just(testProduct));
        when(productRepository.count()).thenReturn(Mono.just(1L));

        Mono<Page<Product>> result = productService.getAll(pageable);

        StepVerifier.create(result)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getContent().getFirst().equals(testProduct))
                .verifyComplete();
    }

    @Test
    void search_shouldFilterProductsByName() {
        String query = "test";
        when(productCacheService.getAllProducts()).thenReturn(Flux.just(testProduct));
        when(productRepository.count()).thenReturn(Mono.just(1L));

        Mono<Page<Product>> result = productService.search(query, pageable);

        StepVerifier.create(result)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getContent().getFirst().equals(testProduct))
                .verifyComplete();
    }

    @Test
    void search_shouldReturnEmptyPageWhenNoMatches() {
        String query = "nonexistent";
        when(productCacheService.getAllProducts()).thenReturn(Flux.just(testProduct));
        when(productRepository.count()).thenReturn(Mono.just(1L));

        Mono<Page<Product>> result = productService.search(query, pageable);

        StepVerifier.create(result)
                .expectNextMatches(page -> page.getContent().isEmpty())
                .verifyComplete();
    }

    @Test
    void getSorted_shouldSortProducts() {
        String sort = "price_asc";
        when(productCacheService.getAllProducts()).thenReturn(Flux.just(testProduct));
        when(productRepository.count()).thenReturn(Mono.just(1L));

        Mono<Page<Product>> result = productService.getSorted(sort, pageable);

        StepVerifier.create(result)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getContent().getFirst().equals(testProduct))
                .verifyComplete();
    }

    @Test
    void getProducts_shouldCombineSearchAndSort() {
        String search = "test";
        String sort = "price_asc";
        when(productCacheService.getAllProducts()).thenReturn(Flux.just(testProduct));

        Mono<Page<Product>> result = productService.getProducts(search, sort, pageable);

        StepVerifier.create(result)
                .expectNextMatches(page ->
                        page.getContent().size() == 1 &&
                                page.getContent().getFirst().equals(testProduct))
                .verifyComplete();
    }

    @Test
    void getByUuid_shouldReturnProductWhenFound() {
        when(productCacheService.getProductById(testUuid)).thenReturn(Mono.just(testProduct));

        Mono<Product> result = productService.getByUuid(testUuid);

        StepVerifier.create(result)
                .expectNext(testProduct)
                .verifyComplete();
    }

    @Test
    void getByUuid_shouldThrowWhenNotFound() {
        when(productCacheService.getProductById(testUuid)).thenReturn(Mono.empty());

        Mono<Product> result = productService.getByUuid(testUuid);

        StepVerifier.create(result)
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    void batchAdd_shouldSaveAndCacheProducts() {
        Product productToSave = new Product();
        when(productMapper.productToProductDao(any())).thenReturn(new ProductDao());
        when(productRepository.saveAll(any(List.class))).thenReturn(Flux.empty());
        when(productCacheService.cacheProducts(any())).thenReturn(Mono.empty());
        when(productCacheService.evictListCache()).thenReturn(Mono.empty());

        Mono<Void> result = productService.batchAdd(Flux.just(productToSave));

        StepVerifier.create(result)
                .verifyComplete();

        verify(productRepository).saveAll(any(List.class));
        verify(productCacheService).cacheProducts(any());
        verify(productCacheService).evictListCache();
    }

    @Test
    void getProductsByIds_shouldReturnCachedProducts() {
        Set<UUID> ids = Collections.singleton(testUuid);
        when(productCacheService.getAllProducts()).thenReturn(Flux.just(testProduct));

        Mono<Map<UUID, Product>> result = productService.getProductsByUuids(ids);

        StepVerifier.create(result)
                .expectNextMatches(map ->
                        map.size() == 1 &&
                                map.get(testUuid).equals(testProduct))
                .verifyComplete();
    }

    @Test
    void getProductsByIds_shouldFetchMissingProductsFromRepository() {
        UUID missingUuid = UUID.randomUUID();
        Set<UUID> ids = Set.of(testUuid, missingUuid);

        Product missingProduct = new Product();
        missingProduct.setUuid(missingUuid);
        missingProduct.setName("Missing Product");
        missingProduct.setPrice(BigDecimal.valueOf(200.0));

        when(productCacheService.getAllProducts()).thenReturn(Flux.just(testProduct));

        ProductDao missingProductDao = new ProductDao();
        when(productRepository.findAllById(Set.of(missingUuid))).thenReturn(Flux.just(missingProductDao));
        when(productMapper.productDaoToProduct(missingProductDao)).thenReturn(missingProduct);

        Mono<Map<UUID, Product>> result = productService.getProductsByUuids(ids);

        StepVerifier.create(result)
                .expectNextMatches(map -> {
                    if (map.size() != 2) {
                        System.out.println("Expected 2 products but got: " + map.size());
                        return false;
                    }

                    boolean hasTestProduct = map.containsKey(testUuid) &&
                            map.get(testUuid).equals(testProduct);
                    boolean hasMissingProduct = map.containsKey(missingUuid) &&
                            map.get(missingUuid).equals(missingProduct);

                    if (!hasTestProduct) {
                        System.out.println("Test product missing or not matching");
                    }
                    if (!hasMissingProduct) {
                        System.out.println("Missing product missing or not matching");
                    }

                    return hasTestProduct && hasMissingProduct;
                })
                .verifyComplete();
    }

    @Test
    void getProductsByUuids_shouldReturnEmptyMapForEmptyInput() {
        Mono<Map<UUID, Product>> result = productService.getProductsByUuids(Collections.emptySet());

        StepVerifier.create(result)
                .expectNextMatches(Map::isEmpty)
                .verifyComplete();
    }
}