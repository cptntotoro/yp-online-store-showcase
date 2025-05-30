package ru.practicum.mapper.product;

import org.mapstruct.Mapper;
import ru.practicum.dto.product.ProductInDto;
import ru.practicum.dto.product.ProductOutDto;
import ru.practicum.model.product.Product;

/**
 * Маппер товаров
 */
@Mapper(componentModel = "spring")
public interface ProductMapper {

    /**
     * Смаппить товар в исходящее DTO товара
     *
     * @param product Товар
     * @return DTO товара
     */
    ProductOutDto productToProductOutDto(Product product);

    /**
     * Смаппить входящее DTO товара в товар
     *
     * @param productInDto Входящее DTO товара
     * @return Товар
     */
    Product productInDtoToProduct(ProductInDto productInDto);
}
