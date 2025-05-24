package ru.practicum.mapper.product;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.practicum.dto.product.ProductDto;
import ru.practicum.model.product.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    Product productToProductDto(ProductDto productDto);
}
