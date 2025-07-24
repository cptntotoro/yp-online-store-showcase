package ru.practicum.controller.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseControllerTest;
import ru.practicum.dto.product.ProductListInDto;
import ru.practicum.dto.product.ProductOutDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.service.product.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductViewControllerTest extends BaseControllerTest {

    @MockBean
    ProductService productService;

    @MockBean
    private ProductMapper productMapper;

    private UUID testProductId;
    private Product testProduct;
    private ProductOutDto testProductDto;

    @BeforeEach
    void setUp() {
        super.baseSetUp();
        testProductId = UUID.randomUUID();
        testProduct = createTestProduct();
        testProductDto = createTestProductDto();
    }

    @Test
    void showProductList_ShouldReturnCatalogPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        when(productService.getProducts(any(), any(), any()))
                .thenReturn(Mono.just(new PageImpl<>(List.of(testProduct), pageable, 1)));
        when(productMapper.productToProductOutDto(any()))
                .thenReturn(testProductDto);

        webTestClient.get()
                .uri("/products?page=0&size=10")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void showProductList_WithSearch_ShouldReturnFilteredResults() {
        String searchQuery = "test";
        PageRequest pageable = PageRequest.of(0, 10);
        when(productService.getProducts(eq(searchQuery), any(), any()))
                .thenReturn(Mono.just(new PageImpl<>(List.of(testProduct), pageable, 1)));
        when(productMapper.productToProductOutDto(any()))
                .thenReturn(testProductDto);

        webTestClient.get()
                .uri("/products?search=test&page=0&size=10")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void showProductList_WithSort_ShouldReturnSortedResults() {
        String sort = "price-asc";
        PageRequest pageable = PageRequest.of(0, 10);
        when(productService.getProducts(any(), eq(sort), any()))
                .thenReturn(Mono.just(new PageImpl<>(List.of(testProduct), pageable, 1)));
        when(productMapper.productToProductOutDto(any()))
                .thenReturn(testProductDto);

        webTestClient.get()
                .uri("/products?sort=price-asc&page=0&size=10")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void showProductDetails_ShouldReturnProductPage() {
        when(productService.getByUuid(testProductId))
                .thenReturn(Mono.just(testProduct));
        when(productMapper.productToProductOutDto(testProduct))
                .thenReturn(testProductDto);

        webTestClient.get()
                .uri("/products/" + testProductId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void showAddProductForm_ShouldReturnAddProductPage() {
        getWebTestClientWithMockUser().get()
                .uri("/products/add")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void showAddProductForm_ShouldRedirect3xx() {
        webTestClient.get()
                .uri("/products/add")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    void addProducts_ShouldRedirectToCatalog_WhenSuccess() {
        ProductListInDto productsDto = new ProductListInDto();
        when(productService.batchAdd(any()))
                .thenReturn(Mono.empty());

        getWebTestClientWithMockUser().post()
                .uri("/products/add")
                .bodyValue(productsDto)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/products");
    }

    private Product createTestProduct() {
        return Product.builder()
                .uuid(testProductId)
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .build();
    }

    private ProductOutDto createTestProductDto() {
        return ProductOutDto.builder()
                .uuid(testProductId)
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .build();
    }
}