package ru.practicum.service.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.practicum.model.product.Product;

import java.util.List;
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
    Page<Product> getAll(Pageable pageable);

    /**
     * Получить страницу товаров по названию
     *
     * @param query Название из запроса
     * @param pageable Страница
     * @return Страница товаров
     */
    Page<Product> search(String query, Pageable pageable);

    /**
     * Получить отсортированную страницу товаров
     *
     * @param sort Тип сортировки
     * @param pageable Страница
     * @return Страница товаров
     */
    Page<Product> getSorted(String sort, Pageable pageable);

    /**
     * Получить товар по идентификатору
     *
     * @param productUuid Идентификатор товара
     * @return Товар
     */
    Product getByUuid(UUID productUuid);

    /**
     * Добавить список товаров
     *
     * @param products Список товаров
     */
    void batchAdd(List<Product> products);
}
