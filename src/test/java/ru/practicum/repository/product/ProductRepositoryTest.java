package ru.practicum.repository.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.practicum.model.product.Product;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product product1;
    private Product product2;
    private Product product3;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        product1 = new Product();
        product1.setName("Умная колонка");
        product1.setPrice(BigDecimal.valueOf(5000));
        product1.setDescription("Умная колонка с голосовым помощником");

        product2 = new Product();
        product2.setName("Смартфон");
        product2.setPrice(BigDecimal.valueOf(30000));
        product2.setDescription("Флагманский смартфон");

        product3 = new Product();
        product3.setName("Умные часы");
        product3.setPrice(BigDecimal.valueOf(10000));
        product3.setDescription("Умные часы с функцией отслеживания здоровья");

        productRepository.saveAll(List.of(product1, product2, product3));
    }

    @Test
    void findByNameContainingIgnoreCase_shouldReturnMatchingProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productRepository.findByNameContainingIgnoreCase("умн", pageable);

        assertThat(result.getContent())
                .hasSize(2)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("Умная колонка", "Умные часы");
    }

    @Test
    void findAllByOrderByPriceAsc_shouldReturnProductsSortedByPriceAsc() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productRepository.findAllByOrderByPriceAsc(pageable);

        assertThat(result.getContent())
                .hasSize(3)
                .extracting(Product::getPrice)
                .containsExactly(
                        BigDecimal.valueOf(5000),
                        BigDecimal.valueOf(10000),
                        BigDecimal.valueOf(30000)
                );
    }

    @Test
    void findAllByOrderByPriceDesc_shouldReturnProductsSortedByPriceDesc() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productRepository.findAllByOrderByPriceDesc(pageable);

        assertThat(result.getContent())
                .hasSize(3)
                .extracting(Product::getPrice)
                .containsExactly(
                        BigDecimal.valueOf(30000),
                        BigDecimal.valueOf(10000),
                        BigDecimal.valueOf(5000)
                );
    }

    @Test
    void findAllByOrderByNameAsc_shouldReturnProductsSortedByName() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productRepository.findAllByOrderByNameAsc(pageable);

        assertThat(result.getContent())
                .hasSize(3)
                .extracting(Product::getName)
                .containsExactly(
                        "Смартфон",
                        "Умная колонка",
                        "Умные часы"
                );
    }

    @Test
    void findByNameContainingIgnoreCase_shouldReturnEmptyPageWhenNoMatches() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> result = productRepository.findByNameContainingIgnoreCase("несуществующий", pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findAllByOrderByPriceAsc_shouldReturnCorrectPage() {
        Pageable pageable = PageRequest.of(0, 2); // Первая страница с 2 элементами
        Page<Product> result = productRepository.findAllByOrderByPriceAsc(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }
}