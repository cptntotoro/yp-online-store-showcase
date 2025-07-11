package ru.practicum.service.product;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.practicum.dao.product.ProductDao;
import ru.practicum.dto.product.cache.ProductCacheDto;
import ru.practicum.exception.product.ProductNotFoundException;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.repository.product.ProductRepository;
import ru.practicum.service.CacheServiceIntegrationTest;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ProductCacheServiceIntegrationTest extends CacheServiceIntegrationTest {

    @Autowired
    private ProductCacheServiceImpl productCacheService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ReactiveRedisTemplate<String, ProductCacheDto> productCacheTemplate;

    @Autowired
    private ReactiveRedisTemplate<String, List<ProductCacheDto>> listCacheTemplate;

    private ProductDao testProductDao;
    private Product testProduct;
    private ProductCacheDto testProductCacheDto;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll().block();
        productCacheTemplate.getConnectionFactory().getReactiveConnection().serverCommands().flushAll().block();

        testProductDao = new ProductDao();
        testProductDao.setName("Test Product");
        testProductDao.setPrice(BigDecimal.valueOf(100.0));

        testProductDao = productRepository.save(testProductDao).block();

        testProduct = productMapper.productDaoToProduct(testProductDao);

        testProductCacheDto = new ProductCacheDto();
        testProductCacheDto.setUuid(testProductDao.getUuid());
        testProductCacheDto.setName("Test Product");
        testProductCacheDto.setPrice(BigDecimal.valueOf(100.0));
    }

    @AfterEach
    void tearDown() {
        productCacheTemplate.getConnectionFactory().getReactiveConnection().serverCommands().flushAll().block();
        productRepository.deleteAll().block();
    }

    @Test
    void getProductById_WhenNotCached_ShouldFetchFromDbAndCache() {
        StepVerifier.create(productCacheService.getProductById(testProductDao.getUuid()))
                .assertNext(p -> {
                    assertEquals(testProductDao.getUuid(), p.getUuid());
                    assertEquals("Test Product", p.getName());
                    assertEquals(0, BigDecimal.valueOf(100.0).compareTo(p.getPrice()));
                })
                .verifyComplete();
    }

    @Test
    void getProductById_WhenCached_ShouldReturnFromCache() {
        productCacheTemplate.opsForValue()
                .set("product:" + testProductDao.getUuid(), testProductCacheDto, Duration.ofMinutes(2))
                .block();

        StepVerifier.create(productCacheService.getProductById(testProductDao.getUuid()))
                .assertNext(p -> {
                    assertEquals(testProductDao.getUuid(), p.getUuid());
                    assertEquals("Test Product", p.getName());
                    assertEquals(0, BigDecimal.valueOf(100.0).compareTo(p.getPrice()));
                })
                .verifyComplete();

        assertEquals(1, productRepository.count().block());
    }

    @Test
    void getProductById_WhenNotFound_ShouldThrowException() {
        UUID nonExistentId = UUID.randomUUID();
        StepVerifier.create(productCacheService.getProductById(nonExistentId))
                .expectError(ProductNotFoundException.class)
                .verify();
    }

    @Test
    void getAllProducts_WhenNotCached_ShouldFetchFromDbAndCache() {
        StepVerifier.create(productCacheService.getAllProducts().collectList())
                .assertNext(products -> {
                    assertEquals(1, products.size());
                    Product p = products.getFirst();
                    assertEquals(testProductDao.getUuid(), p.getUuid());
                    assertEquals("Test Product", p.getName());
                    assertEquals(0, BigDecimal.valueOf(100.0).compareTo(p.getPrice()));
                })
                .verifyComplete();

        StepVerifier.create(listCacheTemplate.opsForValue().get("all_products"))
                .assertNext(dtos -> {
                    assertEquals(1, dtos.size());
                    ProductCacheDto dto = dtos.getFirst();
                    assertEquals(testProductDao.getUuid(), dto.getUuid());
                    assertEquals("Test Product", dto.getName());
                    assertEquals(0, BigDecimal.valueOf(100.0).compareTo(dto.getPrice()));
                })
                .verifyComplete();
    }

    @Test
    void getAllProducts_WhenCached_ShouldReturnFromCache() {
        listCacheTemplate.opsForValue()
                .set("all_products", List.of(testProductCacheDto), Duration.ofMinutes(2))
                .block();

        StepVerifier.create(productCacheService.getAllProducts().collectList())
                .assertNext(products -> {
                    assertEquals(1, products.size());
                    Product p = products.getFirst();
                    assertEquals(testProductDao.getUuid(), p.getUuid());
                    assertEquals("Test Product", p.getName());
                    assertEquals(0, BigDecimal.valueOf(100.0).compareTo(p.getPrice()));
                })
                .verifyComplete();

        assertEquals(1, productRepository.count().block());
    }

    @Test
    void cacheProducts_ShouldSaveToRedis() {
        StepVerifier.create(productCacheService.cacheProducts(List.of(testProduct)))
                .verifyComplete();

        StepVerifier.create(productCacheTemplate.opsForValue().get("product:" + testProductDao.getUuid()))
                .assertNext(dto -> assertEquals(testProductDao.getUuid(), dto.getUuid()))
                .verifyComplete();

        StepVerifier.create(listCacheTemplate.opsForValue().get("all_products"))
                .assertNext(dtos -> {
                    assertEquals(1, dtos.size());
                    assertEquals(testProductDao.getUuid(), dtos.getFirst().getUuid());
                })
                .verifyComplete();
    }

    @Test
    void evictListCache_ShouldRemoveAllProductsKey() {
        listCacheTemplate.opsForValue()
                .set("all_products", List.of(testProductCacheDto), Duration.ofMinutes(2))
                .block();

        StepVerifier.create(productCacheService.evictListCache())
                .verifyComplete();

        StepVerifier.create(listCacheTemplate.opsForValue().get("all_products"))
                .expectNextCount(0)
                .verifyComplete();
    }
}