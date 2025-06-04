package ru.practicum.controller.product;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import ru.practicum.dto.product.ProductInDto;
import ru.practicum.dto.product.ProductListInDto;
import ru.practicum.dto.product.ProductOutDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.service.product.ProductService;

import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductViewControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private Model model;

    @InjectMocks
    private ProductViewController productViewController;

    @Test
    void showProductList_ShouldReturnCatalogView() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> mockPage = new PageImpl<>(List.of(new Product(), new Product()));

        when(productService.getAll(pageable)).thenReturn(mockPage);
        when(productMapper.productToProductOutDto(any(Product.class))).thenReturn(new ProductOutDto());

        String viewName = productViewController.showProductList(page, size, null, null, model);

        assertEquals("product/catalog", viewName);
        verify(productService).getAll(pageable);
        verify(model).addAttribute(eq("products"), any(Page.class));
    }

    @Test
    void showProductList_WithSearch_ShouldUseSearchService() {
        String searchQuery = "test";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> mockPage = new PageImpl<>(List.of(new Product()));

        when(productService.search(searchQuery, pageable)).thenReturn(mockPage);
        when(productMapper.productToProductOutDto(any(Product.class))).thenReturn(new ProductOutDto());

        String viewName = productViewController.showProductList(0, 10, searchQuery, null, model);

        assertEquals("product/catalog", viewName);
        verify(productService).search(searchQuery, pageable);
        verify(productService, never()).getAll(any());
    }

    @Test
    void showProductList_WithSort_ShouldUseSortService() {
        String sortParam = "price,asc";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> mockPage = new PageImpl<>(List.of(new Product()));

        when(productService.getSorted(sortParam, pageable)).thenReturn(mockPage);
        when(productMapper.productToProductOutDto(any(Product.class))).thenReturn(new ProductOutDto());

        String viewName = productViewController.showProductList(0, 10, null, sortParam, model);

        assertEquals("product/catalog", viewName);
        verify(productService).getSorted(sortParam, pageable);
        verify(productService, never()).getAll(any());
    }

    @Test
    void showProductDetails_ShouldReturnProductView() {
        UUID productUuid = UUID.randomUUID();
        Product mockProduct = new Product();
        ProductOutDto mockDto = new ProductOutDto();

        when(productService.getByUuid(productUuid)).thenReturn(mockProduct);
        when(productMapper.productToProductOutDto(mockProduct)).thenReturn(mockDto);

        String viewName = productViewController.showProductDetails(productUuid, model);

        assertEquals("product/product", viewName);
        verify(productService).getByUuid(productUuid);
        verify(model).addAttribute("product", mockDto);
    }

    @Test
    void showAddProductForm_ShouldReturnAddViewWithEmptyDto() {
        String viewName = productViewController.showAddProductForm(model);

        assertEquals("product/add", viewName);
        verify(model).addAttribute(eq("products"), any(ProductListInDto.class));
    }

    @Test
    void addProducts_ShouldProcessBatchAndRedirect() {
        ProductListInDto productsDto = new ProductListInDto();
        productsDto.setProducts(List.of(new ProductInDto(), new ProductInDto()));

        when(productMapper.productInDtoToProduct(any(ProductInDto.class))).thenReturn(new Product());

        String redirectUrl = productViewController.addProducts(productsDto);

        assertEquals("redirect:/products", redirectUrl);
        verify(productService).batchAdd(anyList());
        verify(productMapper, times(2)).productInDtoToProduct(any(ProductInDto.class));
    }
}
