package ru.practicum.repository.product;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.practicum.dao.product.ProductDao;

import java.util.UUID;

/**
 * Репозиторий товаров
 */
@Repository
public interface ProductRepository extends ReactiveCrudRepository<ProductDao, UUID> {

    /**
     * Получить страницу товаров по названию
     *
     * @param name Название
     * @return Страница товаров по названию
     */
    Flux<ProductDao> findByNameContainingIgnoreCase(String name);
//
//    /**
//     * Получить страницу товаров по возрастанию цены
//     *
//     * @return Страница товаров по возрастанию цены
//     */
//    Flux<Product> findAllByOrderByPriceAsc();
//
//    /**
//     * Получить страницу товаров по убыванию цены
//     *
//     * @return Страница товаров по убыванию цены
//     */
//    Flux<Product> findAllByOrderByPriceDesc();
//
//    /**
//     * Получить страницу товаров по алфавиту
//     *
//     * @return Страница товаров по алфавиту
//     */
//    Flux<Product> findAllByOrderByNameAsc();
}
