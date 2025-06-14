package ru.practicum.controller.product;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;
import ru.practicum.controller.BaseControllerTest;
import ru.practicum.dto.product.ProductInDto;
import ru.practicum.dto.product.ProductListInDto;
import ru.practicum.dto.product.ProductOutDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.service.product.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductViewControllerTest extends BaseControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductViewController productViewController;

    private final UUID testProductUuid = UUID.randomUUID();
    private final Product testProduct = new Product();
    private final ProductOutDto testProductDto = new ProductOutDto();

    @Override
    protected Object getController() {
        return productViewController;
    }

    @BeforeEach
    void setUp() {
        super.baseSetUp();
        testProduct.setUuid(testProductUuid);
        testProductDto.setUuid(testProductUuid);
    }

    @Test
    void showProductList_ShouldReturnCatalogView_WithDefaultPagination() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct));
        Page<ProductOutDto> dtoPage = new PageImpl<>(List.of(testProductDto));

        when(productService.getAll(pageable)).thenReturn(Mono.just(productPage));
        when(productMapper.productToProductOutDto(testProduct)).thenReturn(testProductDto);

        webTestClient.get()
                .uri("/products")
                .exchange()
                .expectStatus().isOk();

        verify(productService).getAll(pageable);
        verify(productMapper).productToProductOutDto(testProduct);
    }

    @Test
    void showProductList_ShouldReturnCatalogView_WithSearch() {
        String searchQuery = "test";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct));
        Page<ProductOutDto> dtoPage = new PageImpl<>(List.of(testProductDto));

        when(productService.search(searchQuery, pageable)).thenReturn(Mono.just(productPage));
        when(productMapper.productToProductOutDto(testProduct)).thenReturn(testProductDto);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/products")
                        .queryParam("search", searchQuery)
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(productService).search(searchQuery, pageable);
        verify(productMapper).productToProductOutDto(testProduct);
    }

    @Test
    void showProductList_ShouldReturnCatalogView_WithSorting() {
        String sortBy = "price";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(testProduct));
        Page<ProductOutDto> dtoPage = new PageImpl<>(List.of(testProductDto));

        when(productService.getSorted(sortBy, pageable)).thenReturn(Mono.just(productPage));
        when(productMapper.productToProductOutDto(testProduct)).thenReturn(testProductDto);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/products")
                        .queryParam("sort", sortBy)
                        .build())
                .exchange()
                .expectStatus().isOk();

        verify(productService).getSorted(sortBy, pageable);
        verify(productMapper).productToProductOutDto(testProduct);
    }

    @Test
    void showProductDetails_ShouldReturnProductView_WhenProductExists() {
        when(productService.getByUuid(testProductUuid)).thenReturn(Mono.just(testProduct));
        when(productMapper.productToProductOutDto(testProduct)).thenReturn(testProductDto);

        webTestClient.get()
                .uri("/products/" + testProductUuid)
                .exchange()
                .expectStatus().isOk();

        verify(productService).getByUuid(testProductUuid);
        verify(productMapper).productToProductOutDto(testProduct);
    }

    @Test
    void showAddProductForm_ShouldReturnAddProductView() {
        webTestClient.get()
                .uri("/products/add")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void addProducts_ShouldRedirectToProductList_WhenSuccess() {
        ProductListInDto productListInDto = new ProductListInDto();
        productListInDto.setProducts(List.of(ProductInDto.builder()
                .name("Product")
                .price(BigDecimal.TEN)
                .description("Product Description")
                .imageUrl("URL")
                .build()));

        when(productService.batchAdd(any())).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/products/add")
                .bodyValue(productListInDto)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/products");

        verify(productService).batchAdd(any());
    }
}
