package ru.practicum.service.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.product.ProductNotFoundException;
import ru.practicum.model.product.Product;
import ru.practicum.repository.product.ProductRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    /**
     * Репозиторий товаров
     */
    private final ProductRepository productRepository;

    @Override
    public Page<Product> getAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public Page<Product> search(String query, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCase(query, pageable);
    }

    @Override
    public Page<Product> getSorted(String sort, Pageable pageable) {
        return switch (sort) {
            case "price-asc" -> productRepository.findAllByOrderByPriceAsc(pageable);
            case "price-desc" -> productRepository.findAllByOrderByPriceDesc(pageable);
            case "name-asc" -> productRepository.findAllByOrderByNameAsc(pageable);
            default -> productRepository.findAll(pageable);
        };
    }

    @Override
    public Product getByUuid(UUID uuid) {
        return productRepository.findById(uuid).orElseThrow(() -> new ProductNotFoundException("Товар не найден"));
    }

    @Override
    public void batchAdd(List<Product> products) {
        productRepository.saveAll(products);
    }
}
