package ru.practicum.service.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.product.Product;

import java.util.UUID;

/**
 * Сервис управления товарами
 */
public interface ProductService {

    /**
     * Получить страницу товаров
     *
     * @param pageable Страница
     * @return Страница товаров
     */
    Mono<Page<Product>> getAll(Pageable pageable);

    /**
     * Получить страницу товаров по названию
     *
     * @param query Название из запроса
     * @param pageable Страница
     * @return Страница товаров
     */
    Mono<Page<Product>> search(String query, Pageable pageable);

    /**
     * Получить отсортированную страницу товаров
     *
     * @param sort Тип сортировки
     * @param pageable Страница
     * @return Страница товаров
     */
    Mono<Page<Product>> getSorted(String sort, Pageable pageable);

    /**
     * Получить товар по идентификатору
     *
     * @param productUuid Идентификатор товара
     * @return Товар
     */
    Mono<Product> getByUuid(UUID productUuid);

    /**
     * Добавить список товаров
     *
     * @param products Список товаров
     */
    Mono<Void> batchAdd(Flux<Product> products);
}
