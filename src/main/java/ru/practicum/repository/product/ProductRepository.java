package ru.practicum.repository.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.model.product.Product;

import java.util.UUID;

/**
 * Репозиторий товаров
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {

    /**
     * Получить страницу товаров по названию
     *
     * @param name Название
     * @param pageable Страница
     * @return Страница товаров по названию
     */
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Получить страницу товаров по возрастанию цены
     *
     * @param pageable Страница
     * @return Страница товаров по возрастанию цены
     */
    Page<Product> findAllByOrderByPriceAsc(Pageable pageable);

    /**
     * Получить страницу товаров по убыванию цены
     *
     * @param pageable Страница
     * @return Страница товаров по убыванию цены
     */
    Page<Product> findAllByOrderByPriceDesc(Pageable pageable);

    /**
     * Получить страницу товаров по алфавиту
     *
     * @param pageable Страница
     * @return Страница товаров по алфавиту
     */
    Page<Product> findAllByOrderByNameAsc(Pageable pageable);
}
