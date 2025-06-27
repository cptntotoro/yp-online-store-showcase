package ru.practicum.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Список входящих DTO товаров
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductListInDto {
    /**
     * Список входящих DTO товаров
     */
    private List<ProductInDto> products = new ArrayList<>();
}
