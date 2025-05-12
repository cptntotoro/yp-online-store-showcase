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
import ru.practicum.model.product.Product;
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
        List<Product> products = List.of(new Product(), new Product());
        Page<Product> expectedPage = new PageImpl<>(products);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Product> result = productService.getAll(pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void search_shouldReturnFilteredProducts() {
        String query = "test";
        List<Product> products = List.of(new Product());
        Page<Product> expectedPage = new PageImpl<>(products);
        when(productRepository.findByNameContainingIgnoreCase(eq(query), any(Pageable.class))).thenReturn(expectedPage);

        Page<Product> result = productService.search(query, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findByNameContainingIgnoreCase(query, pageable);
    }

    @Test
    void getSorted_withPriceAsc_shouldReturnSortedProducts() {
        String sort = "price-asc";
        List<Product> products = List.of(new Product());
        Page<Product> expectedPage = new PageImpl<>(products);
        when(productRepository.findAllByOrderByPriceAsc(any(Pageable.class))).thenReturn(expectedPage);

        Page<Product> result = productService.getSorted(sort, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAllByOrderByPriceAsc(pageable);
    }

    @Test
    void getSorted_withPriceDesc_shouldReturnSortedProducts() {
        String sort = "price-desc";
        List<Product> products = List.of(new Product());
        Page<Product> expectedPage = new PageImpl<>(products);
        when(productRepository.findAllByOrderByPriceDesc(any(Pageable.class))).thenReturn(expectedPage);

        Page<Product> result = productService.getSorted(sort, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAllByOrderByPriceDesc(pageable);
    }

    @Test
    void getSorted_withNameAsc_shouldReturnSortedProducts() {
        String sort = "name-asc";
        List<Product> products = List.of(new Product());
        Page<Product> expectedPage = new PageImpl<>(products);
        when(productRepository.findAllByOrderByNameAsc(any(Pageable.class))).thenReturn(expectedPage);

        Page<Product> result = productService.getSorted(sort, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAllByOrderByNameAsc(pageable);
    }

    @Test
    void getSorted_withUnknownSort_shouldReturnDefaultSortedProducts() {
        String sort = "unknown";
        List<Product> products = List.of(new Product());
        Page<Product> expectedPage = new PageImpl<>(products);
        when(productRepository.findAll(any(Pageable.class))).thenReturn(expectedPage);

        Page<Product> result = productService.getSorted(sort, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void getByUuid_whenProductExists_shouldReturnProduct() {
        UUID uuid = UUID.randomUUID();
        Product expectedProduct = new Product();
        when(productRepository.findById(uuid)).thenReturn(Optional.of(expectedProduct));

        Product result = productService.getByUuid(uuid);

        assertNotNull(result);
        assertEquals(expectedProduct, result);
        verify(productRepository).findById(uuid);
    }

    @Test
    void getByUuid_whenProductNotExists_shouldThrowException() {
        UUID uuid = UUID.randomUUID();
        when(productRepository.findById(uuid)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.getByUuid(uuid));
        verify(productRepository).findById(uuid);
    }

}
