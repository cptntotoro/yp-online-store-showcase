package ru.practicum.service.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.exception.cart.CartNotFoundException;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.repository.cart.CartRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartCacheServiceImpl implements CartCacheService {

    /**
     * Репозиторий корзины товаров
     */
    private final CartRepository cartRepository;

    /**
     * Маппер корзины товаров
     */
    private final CartMapper cartMapper;

    @Cacheable(value = "cart", key = "#userUuid")
    @Override
    public Mono<Cart> getCart(UUID userUuid) {
        return cartRepository.findByUserUuid(userUuid)
                .map(cartMapper::cartDaoToCart)
                .switchIfEmpty(Mono.error(new CartNotFoundException("Корзина пользователя с UUID = " + userUuid + " не найдена")))
                .cache();
    }

    @CacheEvict(value = "cart", key = "#userUuid")
    @Override
    public void evict(UUID userUuid) {
    }
}
