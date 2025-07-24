package ru.practicum.controller.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dto.product.ProductListInDto;
import ru.practicum.mapper.product.ProductMapper;
import ru.practicum.model.product.Product;
import ru.practicum.service.product.ProductService;

import java.util.List;
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
    public Mono<String> showProductList(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size,
                                        @RequestParam(required = false) String search,
                                        @RequestParam(required = false) String sort,
                                        Model model) {

      return productService.getProducts(search, sort, PageRequest.of(page, size))
                .map(productPage -> {
                    model.addAttribute("products", productPage.map(productMapper::productToProductOutDto));
                    model.addAttribute("search", search);
                    model.addAttribute("sort", sort);

                    model.addAttribute("totalPages", productPage.getTotalPages());
                    model.addAttribute("currentPage", productPage.getNumber());

                    return "product/catalog";
                });
    }

    @GetMapping("/{uuid}")
    public Mono<String> showProductDetails(@PathVariable("uuid") UUID uuid, Model model) {
        return productService.getByUuid(uuid)
                .map(productMapper::productToProductOutDto)
                .doOnNext(dto -> model.addAttribute("product", dto))
                .thenReturn("product/product");
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/add")
    public Mono<String> showAddProductForm(Model model) {
        return Mono.just(model.addAttribute("products", new ProductListInDto()))
                .thenReturn("product/add");
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/add")
    public Mono<String> addProducts(@ModelAttribute("products") ProductListInDto products) {
        List<Product> productList = products.getProducts().stream()
                .map(productMapper::productInDtoToProduct)
                .toList();

        return productService.batchAdd(Flux.fromIterable(productList))
                .then(Mono.just("redirect:/products"));
    }
}
