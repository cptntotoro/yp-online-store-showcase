package ru.practicum.service.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.exception.product.ProductNotFoundException;
import ru.practicum.model.Product;
import ru.practicum.repository.product.ProductRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private Pageable pageable;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void getAll_shouldReturnPageOfProducts() {
        // given
        List<Product> products = List.of(new Product(), new Product());
        Page<Product> expectedPage = new PageImpl<>(products);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        // when
        Page<Product> result = productService.getAll(pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void search_shouldReturnFilteredProducts() {
        // given
        String query = "test";
        List<Product> products = List.of(new Product());
        Page<Product> expectedPage = new PageImpl<>(products);
        when(productRepository.findByNameContainingIgnoreCase(eq(query), any(Pageable.class))).thenReturn(expectedPage);

        // when
        Page<Product> result = productService.search(query, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findByNameContainingIgnoreCase(query, pageable);
    }

    @Test
    void getSorted_withPriceAsc_shouldReturnSortedProducts() {
        // given
        String sort = "price-asc";
        List<Product> products = List.of(new Product());
        Page<Product> expectedPage = new PageImpl<>(products);
        when(productRepository.findAllByOrderByPriceAsc(any(Pageable.class))).thenReturn(expectedPage);

        // when
        Page<Product> result = productService.getSorted(sort, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAllByOrderByPriceAsc(pageable);
    }

    @Test
    void getSorted_withPriceDesc_shouldReturnSortedProducts() {
        // given
        String sort = "price-desc";
        List<Product> products = List.of(new Product());
        Page<Product> expectedPage = new PageImpl<>(products);
        when(productRepository.findAllByOrderByPriceDesc(any(Pageable.class))).thenReturn(expectedPage);

        // when
        Page<Product> result = productService.getSorted(sort, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAllByOrderByPriceDesc(pageable);
    }

    @Test
    void getSorted_withNameAsc_shouldReturnSortedProducts() {
        // given
        String sort = "name-asc";
        List<Product> products = List.of(new Product());
        Page<Product> expectedPage = new PageImpl<>(products);
        when(productRepository.findAllByOrderByNameAsc(any(Pageable.class))).thenReturn(expectedPage);

        // when
        Page<Product> result = productService.getSorted(sort, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAllByOrderByNameAsc(pageable);
    }

    @Test
    void getSorted_withUnknownSort_shouldReturnDefaultSortedProducts() {
        // given
        String sort = "unknown";
        List<Product> products = List.of(new Product());
        Page<Product> expectedPage = new PageImpl<>(products);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        // when
        Page<Product> result = productService.getSorted(sort, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void getByUuid_whenProductExists_shouldReturnProduct() {
        // given
        UUID uuid = UUID.randomUUID();
        Product expectedProduct = new Product();
        when(productRepository.findById(uuid)).thenReturn(Optional.of(expectedProduct));

        // when
        Product result = productService.getByUuid(uuid);

        // then
        assertNotNull(result);
        assertEquals(expectedProduct, result);
        verify(productRepository).findById(uuid);
    }

    @Test
    void getByUuid_whenProductNotExists_shouldThrowException() {
        // given
        UUID uuid = UUID.randomUUID();
        when(productRepository.findById(uuid)).thenReturn(Optional.empty());

        // when & then
        assertThrows(ProductNotFoundException.class, () -> productService.getByUuid(uuid));
        verify(productRepository).findById(uuid);
    }

    @Test
    void add_shouldSaveAndReturnProduct() {
        // given
        Product productToSave = new Product();
        Product savedProduct = new Product();
        when(productRepository.save(productToSave)).thenReturn(savedProduct);

        // when
        Product result = productService.add(productToSave);

        // then
        assertNotNull(result);
        assertEquals(savedProduct, result);
        verify(productRepository).save(productToSave);
    }
}
