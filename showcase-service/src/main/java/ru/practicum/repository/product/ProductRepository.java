package ru.practicum.repository.product;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.dao.product.ProductDao;

import java.util.UUID;

/**
 * Репозиторий товаров
 */
@Repository
public interface ProductRepository extends ReactiveCrudRepository<ProductDao, UUID> {
}
