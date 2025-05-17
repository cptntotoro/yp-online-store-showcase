package ru.practicum.service.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.model.product.Product;

import java.util.UUID;

/**
 * Сервис управления товарами
 */
public interface ProductService {
    Page<Product> getAll(Pageable pageable);

    Page<Product> search(String query, Pageable pageable);

    Page<Product> getSorted(String sort, Pageable pageable);

    Product getByUuid(UUID uuid);

    Product add(Product product);
}
