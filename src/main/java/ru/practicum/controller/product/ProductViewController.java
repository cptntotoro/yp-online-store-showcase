package ru.practicum.controller.product;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.service.product.ProductService;

import java.util.UUID;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductViewController {
    private final ProductService productService;
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

        model.addAttribute("products", productPage.map(productMapper::productToProductDto));
        return "product/catalog";
    }

    @GetMapping("/{uuid}")
    public String showProductDetails(@PathVariable("uuid") UUID uuid, Model model) {
        Product product = productService.getByUuid(uuid);
        model.addAttribute("product", product);
        return "product/product";
    }
}
