package ru.practicum.mapper.product;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
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
}