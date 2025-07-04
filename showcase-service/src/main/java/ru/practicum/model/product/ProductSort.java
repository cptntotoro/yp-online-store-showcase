package ru.practicum.model.product;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 * Сортировки товаров
 */
public enum ProductSort {
    PRICE_ASC("price-asc") {
        @Override
        public Comparator<Product> getComparator() {
            return Comparator.comparing(Product::getPrice);
        }
    },
    PRICE_DESC("price-desc") {
        @Override
        public Comparator<Product> getComparator() {
            return Comparator.comparing(Product::getPrice).reversed();
        }
    },
    NAME_ASC("name-asc") {
        @Override
        public Comparator<Product> getComparator() {
            return Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER);
        }
    },
    NAME_DESC("name-desc") {
        @Override
        public Comparator<Product> getComparator() {
            return Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER).reversed();
        }
    };

    private final String value;

    ProductSort(String value) {
        this.value = value;
    }

    public abstract Comparator<Product> getComparator();

    public static Optional<ProductSort> fromString(String value) {
        return Arrays.stream(values())
                .filter(opt -> opt.value.equalsIgnoreCase(value))
                .findFirst();
    }
}