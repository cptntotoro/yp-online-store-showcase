package ru.practicum.controller.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.product.ProductListInDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.service.product.ProductService;

import java.util.UUID;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductViewController {
    /**
     * Сервис управления товарами
     */
    private final ProductService productService;

    /**
     * Маппер товаров
     */
    private final ProductMapper productMapper;

    @GetMapping
    public String showProductList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            Model model) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage;

        if (search != null && !search.isEmpty()) {
            productPage = productService.search(search, pageable);
        } else if (sort != null && !sort.isEmpty()) {
            productPage = productService.getSorted(sort, pageable);
        } else {
            productPage = productService.getAll(pageable);
        }

        model.addAttribute("products", productPage.map(productMapper::productToProductOutDto));
        return "product/catalog";
    }

    @GetMapping("/{uuid}")
    public String showProductDetails(@PathVariable("uuid") UUID uuid, Model model) {
        Product product = productService.getByUuid(uuid);
        model.addAttribute("product", productMapper.productToProductOutDto(product));
        return "product/product";
    }

    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        model.addAttribute("products", new ProductListInDto());
        return "product/add";
    }

    @PostMapping("/add")
    public String addProducts(@ModelAttribute("products") ProductListInDto products) {
        productService.batchAdd(products.getProducts().stream().map(productMapper::productInDtoToProduct).toList());
        return "redirect:/products";
    }
}
