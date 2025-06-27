package ru.practicum.mapper.product;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dao.product.ProductDao;
import ru.practicum.dto.product.ProductCacheDto;
import ru.practicum.dto.product.ProductInDto;
import ru.practicum.dto.product.ProductOutDto;
import ru.practicum.model.product.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ProductMapperTest {

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Test
    void shouldMapProductToDto() {
        Product product = new Product();
        product.setUuid(UUID.randomUUID());
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(99.99));
        product.setImageUrl("http://test.com/image.jpg");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        ProductOutDto dto = mapper.productToProductOutDto(product);

        assertThat(dto).isNotNull();
        assertThat(dto.getUuid()).isEqualTo(product.getUuid());
        assertThat(dto.getName()).isEqualTo("Test Product");
        assertThat(dto.getPrice()).isEqualTo(BigDecimal.valueOf(99.99));
    }

    @Test
    void shouldMapProductInDtoToProduct() {
        ProductInDto productInDto = new ProductInDto();
        productInDto.setName("Test Product");
        productInDto.setDescription("Test Description");
        productInDto.setPrice(BigDecimal.valueOf(99.99));
        productInDto.setImageUrl("http://test.com/image.jpg");

        Product product = mapper.productInDtoToProduct(productInDto);

        assertThat(product).isNotNull();
        assertThat(product.getName()).isEqualTo(productInDto.getName());
        assertThat(product.getDescription()).isEqualTo(productInDto.getDescription());
        assertThat(product.getPrice()).isEqualTo(productInDto.getPrice());
        assertThat(product.getImageUrl()).isEqualTo(productInDto.getImageUrl());
        assertThat(product.getUuid()).isNull();
        assertThat(product.getCreatedAt()).isNull();
        assertThat(product.getUpdatedAt()).isNull();
    }

    @Test
    void shouldReturnNullWhenProductInDtoIsNull() {
        Product product = mapper.productInDtoToProduct(null);

        assertThat(product).isNull();
    }

    @Test
    void shouldMapProductDaoToProduct() {
        ProductDao productDao = new ProductDao();
        productDao.setUuid(UUID.randomUUID());
        productDao.setName("Test Product");
        productDao.setDescription("Test Description");
        productDao.setPrice(BigDecimal.valueOf(99.99));
        productDao.setImageUrl("http://test.com/image.jpg");
        productDao.setCreatedAt(LocalDateTime.now());
        productDao.setUpdatedAt(LocalDateTime.now());

        Product product = mapper.productDaoToProduct(productDao);

        assertThat(product).isNotNull();
        assertThat(product.getUuid()).isEqualTo(productDao.getUuid());
        assertThat(product.getName()).isEqualTo(productDao.getName());
        assertThat(product.getDescription()).isEqualTo(productDao.getDescription());
        assertThat(product.getPrice()).isEqualTo(productDao.getPrice());
        assertThat(product.getImageUrl()).isEqualTo(productDao.getImageUrl());
        assertThat(product.getCreatedAt()).isEqualTo(productDao.getCreatedAt());
        assertThat(product.getUpdatedAt()).isEqualTo(productDao.getUpdatedAt());
    }

    @Test
    void shouldMapProductToProductDao() {
        Product product = new Product();
        product.setUuid(UUID.randomUUID());
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(99.99));
        product.setImageUrl("http://test.com/image.jpg");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        ProductDao productDao = mapper.productToProductDao(product);

        assertThat(productDao).isNotNull();
        assertThat(productDao.getUuid()).isEqualTo(product.getUuid());
        assertThat(productDao.getName()).isEqualTo(product.getName());
        assertThat(productDao.getDescription()).isEqualTo(product.getDescription());
        assertThat(productDao.getPrice()).isEqualTo(product.getPrice());
        assertThat(productDao.getImageUrl()).isEqualTo(product.getImageUrl());
        assertThat(productDao.getCreatedAt()).isEqualTo(product.getCreatedAt());
        assertThat(productDao.getUpdatedAt()).isEqualTo(product.getUpdatedAt());
    }

    @Test
    void shouldMapProductToCacheDto() {
        Product product = new Product();
        product.setUuid(UUID.randomUUID());
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(BigDecimal.valueOf(99.99));
        product.setImageUrl("http://test.com/image.jpg");
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        ProductCacheDto cacheDto = mapper.productToCacheDto(product);

        assertThat(cacheDto).isNotNull();
        assertThat(cacheDto.getUuid()).isEqualTo(product.getUuid());
        assertThat(cacheDto.getName()).isEqualTo(product.getName());
        assertThat(cacheDto.getDescription()).isEqualTo(product.getDescription());
        assertThat(cacheDto.getPrice()).isEqualTo(product.getPrice());
        assertThat(cacheDto.getImageUrl()).isEqualTo(product.getImageUrl());
    }

    @Test
    void shouldMapCacheDtoToProduct() {
        ProductCacheDto cacheDto = new ProductCacheDto();
        cacheDto.setUuid(UUID.randomUUID());
        cacheDto.setName("Test Product");
        cacheDto.setDescription("Test Description");
        cacheDto.setPrice(BigDecimal.valueOf(99.99));
        cacheDto.setImageUrl("http://test.com/image.jpg");

        Product product = mapper.productCacheDtoToProduct(cacheDto);

        assertThat(product).isNotNull();
        assertThat(product.getUuid()).isEqualTo(cacheDto.getUuid());
        assertThat(product.getName()).isEqualTo(cacheDto.getName());
        assertThat(product.getDescription()).isEqualTo(cacheDto.getDescription());
        assertThat(product.getPrice()).isEqualTo(cacheDto.getPrice());
        assertThat(product.getImageUrl()).isEqualTo(cacheDto.getImageUrl());
        assertThat(product.getCreatedAt()).isNull();
        assertThat(product.getUpdatedAt()).isNull();
    }

    @Test
    void shouldReturnNullWhenMappingNullInput() {
        assertThat(mapper.productDaoToProduct(null)).isNull();
        assertThat(mapper.productToProductDao(null)).isNull();
        assertThat(mapper.productToCacheDto(null)).isNull();
        assertThat(mapper.productCacheDtoToProduct(null)).isNull();
    }
}