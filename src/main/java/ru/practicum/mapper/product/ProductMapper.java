package ru.practicum.mapper.product;

import org.mapstruct.Mapper;
import ru.practicum.dto.product.ProductDto;
import ru.practicum.model.product.Product;

/**
 * Маппер товаров
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /**
     * Смаппить товар в DTO товара
     * @param product Товар
     * @return DTO товара
     */
    ProductDto productToProductDto(Product product);
}
