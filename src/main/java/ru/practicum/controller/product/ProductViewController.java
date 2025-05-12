package ru.practicum.controller.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.model.Product;
import ru.practicum.service.product.ProductService;

import java.util.UUID;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductViewController {
    private final ProductService productService;

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

        model.addAttribute("products", productPage);
        return "product/catalog";
    }

    @GetMapping("/{uuid}")
    public String showProductDetails(@PathVariable UUID uuid, Model model) {
        Product product = productService.getByUuid(uuid);
        model.addAttribute("product", product);
        return "product/product";
    }
}
