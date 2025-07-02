package ru.practicum.service.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.model.product.Product;

import java.util.Map;
import java.util.Set;
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
     * @param query    Название из запроса
     * @param pageable Страница
     * @return Страница товаров
     */
    Mono<Page<Product>> search(String query, Pageable pageable);

    /**
     * Получить отсортированную страницу товаров
     *
     * @param sort     Тип сортировки
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

    /**
     * Получить мапу товаров по их идентификаторам
     *
     * @param productIds набор идентификаторов товаров
     * @return Map с товарами (UUID -> Product)
     */
    Mono<Map<UUID, Product>> getProductsByUuids(Set<UUID> productIds);

    /**
     * Получить товары по поисковому запросу и фильтрам
     *
     * @param search   Поисковый запрос по названию
     * @param sort     Сортировка
     * @param pageable Страница
     * @return Страница товаров
     */
    Mono<Page<Product>> getProducts(String search, String sort, Pageable pageable);
}
