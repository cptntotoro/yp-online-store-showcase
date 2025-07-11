package ru.practicum.mapper.product;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dao.product.ProductDao;
import ru.practicum.dto.product.ProductInDto;
import ru.practicum.dto.product.ProductOutDto;
import ru.practicum.dto.product.cache.ProductCacheDto;
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
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product productInDtoToProduct(ProductInDto productInDto);

    /**
     * Смаппить товар в DAO товара
     *
     * @param productDao DAO товара
     * @return Товар
     */
    Product productDaoToProduct(ProductDao productDao);

    /**
     * Смаппить DAO товара в товар
     *
     * @param product Товар
     * @return DAO товара
     */
    ProductDao productToProductDao(Product product);

    /**
     * Смаппить товар в DTO кеша товаров
     *
     * @param product Товар
     * @return DTO кеша товаров
     */
    ProductCacheDto productToCacheDto(Product product);

    /**
     * Смаппить DTO кеша товаров в товар
     *
     * @param productCacheDto DTO кеша товаров
     * @return Товар
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product productCacheDtoToProduct(ProductCacheDto productCacheDto);
}
