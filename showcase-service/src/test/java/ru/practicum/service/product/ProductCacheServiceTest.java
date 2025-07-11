package ru.practicum.service.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.product.ProductDao;
import ru.practicum.dto.product.cache.ProductCacheDto;
import ru.practicum.exception.product.ProductNotFoundException;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.repository.product.ProductRepository;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductCacheServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ReactiveRedisTemplate<String, ProductCacheDto> productCacheTemplate;

    @Mock
    private ReactiveRedisTemplate<String, List<ProductCacheDto>> listCacheTemplate;

    @Mock
    private ReactiveValueOperations<String, ProductCacheDto> productValueOps;

    @Mock
    private ReactiveValueOperations<String, List<ProductCacheDto>> listValueOps;

    // @InjectMocks какой-то конфликт с внедрением productValueOps и listValueOps
    // вместо listValueOps внедряется productValueOps
    // поэтому внедряем зависимости руками в setUp
    private ProductCacheServiceImpl productCacheService;

    private final UUID productId = UUID.randomUUID();
    private final Product product = new Product();
    private final ProductDao productDao = new ProductDao();
    private ProductCacheDto productCacheDto = new ProductCacheDto();

    @BeforeEach
    void setUp() {
        productCacheService = new ProductCacheServiceImpl(
                productRepository,
                productMapper,
                productCacheTemplate,
                listCacheTemplate
        );

        product.setUuid(productId);
        productCacheDto.setUuid(productId);
        productDao.setUuid(productId);

        lenient().when(listCacheTemplate.opsForValue()).thenReturn(listValueOps);
        lenient().when(productCacheTemplate.opsForValue()).thenReturn(productValueOps);

        lenient().when(productValueOps.get(anyString())).thenReturn(Mono.empty());
        lenient().when(listValueOps.get(anyString())).thenReturn(Mono.empty());
        lenient().when(productRepository.findById(any(UUID.class))).thenReturn(Mono.empty());
        lenient().when(productRepository.findAll()).thenReturn(Flux.empty());

        lenient().when(productValueOps.set(anyString(), any(ProductCacheDto.class), any(Duration.class)))
                .thenReturn(Mono.just(true));
        lenient().when(listValueOps.set(anyString(), anyList(), any(Duration.class)))
                .thenReturn(Mono.just(true));

        lenient().when(listCacheTemplate.delete(anyString())).thenReturn(Mono.just(1L));
    }

    @Test
    void getProductById_WhenCached_ShouldReturnFromCache() {
        when(productValueOps.get("product:" + productId)).thenReturn(Mono.just(productCacheDto));
        when(productMapper.productCacheDtoToProduct(productCacheDto)).thenReturn(product);

        Mono<Product> result = productCacheService.getProductById(productId);

        assertEquals(product, result.block());
    }

    @Test
    void getProductById_WhenNotCached_ShouldFetchAndCache() {
        when(productRepository.findById(productId)).thenReturn(Mono.just(productDao));
        when(productMapper.productDaoToProduct(productDao)).thenReturn(product);
        when(productMapper.productToCacheDto(product)).thenReturn(productCacheDto);

        Mono<Product> result = productCacheService.getProductById(productId);

        Product actualProduct = result.block();
        assertNotNull(actualProduct);
        assertEquals(productId, actualProduct.getUuid());
        verify(productValueOps).set(eq("product:" + productId), eq(productCacheDto), any(Duration.class));
    }

    @Test
    void getProductById_WhenNotFound_ShouldThrowException() {
        when(productRepository.findById(productId)).thenReturn(Mono.empty());

        assertThrows(ProductNotFoundException.class, () ->
                productCacheService.getProductById(productId).block());
    }

    @Test
    void getAllProducts_WhenCached_ShouldReturnFromCache() {
        List<ProductCacheDto> cachedList = List.of(productCacheDto);
        when(listValueOps.get("all_products")).thenReturn(Mono.just(cachedList));
        when(productMapper.productCacheDtoToProduct(productCacheDto)).thenReturn(product);

        Flux<Product> result = productCacheService.getAllProducts();

        List<Product> products = result.collectList().block();
        assertNotNull(products);
        assertEquals(1, products.size());
        assertEquals(product, products.getFirst());
        verify(productRepository, never()).findAll();
    }

    @Test
    void getAllProducts_WhenNotCached_ShouldFetchAndCache() {
        when(productRepository.findAll()).thenReturn(Flux.just(productDao));
        when(productMapper.productDaoToProduct(productDao)).thenReturn(product);
        when(productMapper.productToCacheDto(product)).thenReturn(productCacheDto);

        Flux<Product> result = productCacheService.getAllProducts();

        List<Product> products = result.collectList().block();
        assertNotNull(products);
        assertEquals(1, products.size());
        verify(listValueOps).set(eq("all_products"), anyList(), any(Duration.class));
    }

    @Test
    void cacheProducts_WhenEmptyList_ShouldDoNothing() {
        Mono<Void> result = productCacheService.cacheProducts(List.of());

        assertNull(result.block());
        verifyNoInteractions(productValueOps);
        verifyNoInteractions(listValueOps);
    }

    @Test
    void cacheProducts_ShouldSaveToCache() {
        List<Product> products = List.of(product);
        List<ProductCacheDto> dtos = List.of(productCacheDto);

        when(productMapper.productToCacheDto(product)).thenReturn(productCacheDto);

        Mono<Void> result = productCacheService.cacheProducts(products);

        assertNull(result.block());
        verify(productValueOps).set(eq("product:" + productId), eq(productCacheDto), any(Duration.class));
        verify(listValueOps).set(eq("all_products"), eq(dtos), any(Duration.class));
    }

    @Test
    void evictListCache_ShouldDeleteAllProductsKey() {
        Mono<Void> result = productCacheService.evictListCache();

        assertNull(result.block());
        verify(listCacheTemplate).delete("all_products");
    }
}