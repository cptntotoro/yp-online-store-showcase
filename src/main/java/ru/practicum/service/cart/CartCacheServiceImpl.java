package ru.practicum.service.cart;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.exception.cart.CartNotFoundException;
import ru.practicum.model.cart.Cart;
import ru.practicum.repository.cart.CartRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartCacheServiceImpl implements CartCacheService {

    private final CartRepository cartRepository;

    @Cacheable(value = "cart", key = "#userUuid")
    @Override
    public Mono<Cart> getCart(UUID userUuid) {
        return cartRepository.findByUserUuid(userUuid)
                .switchIfEmpty(Mono.error(new CartNotFoundException("Корзина пользователя с UUID = " + userUuid + " не найдена")))
                .cache();
    }

    @CacheEvict(value = "cart", key = "#userUuid")
    @Override
    public void evict(UUID userUuid) {
    }
}
