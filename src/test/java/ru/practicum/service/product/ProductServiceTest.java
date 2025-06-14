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
import java.util.List;
import java.util.UUID;

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
                    assertEquals(sampleProduct.getName(), page.getContent().get(0).getName());
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
                    assertEquals("Test Product", page.getContent().get(0).getName());
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
                    assertEquals(sampleProduct.getPrice(), page.getContent().get(0).getPrice());
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
        ProductDao dao = ProductDao.builder()
                .uuid(sampleProduct.getUuid())
                .name(sampleProduct.getName())
                .description(sampleProduct.getDescription())
                .price(sampleProduct.getPrice())
                .createdAt(sampleProduct.getCreatedAt())
                .updatedAt(sampleProduct.getUpdatedAt())
                .imageUrl(sampleProduct.getImageUrl())
                .build();

        when(productMapper.productToProductDao(sampleProduct)).thenReturn(dao);
        when(productRepository.saveAll(any(Flux.class))).thenAnswer(invocation -> {
            Flux<ProductDao> argument = invocation.getArgument(0);
            return argument.map(item -> dao); // Эмулируем сохранение
        });

        Flux<Product> inputFlux = Flux.just(sampleProduct);
        Mono<Void> result = productService.batchAdd(inputFlux);

        StepVerifier.create(result)
                .verifyComplete();
        // Проверка вызовов
        verify(productMapper).productToProductDao(sampleProduct);
        verify(productRepository).saveAll(any(Flux.class));
    }
}
