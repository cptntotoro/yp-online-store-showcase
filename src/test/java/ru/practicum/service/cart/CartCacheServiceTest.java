package ru.practicum.service.cart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.cart.CartDao;
import ru.practicum.exception.cart.CartNotFoundException;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.repository.cart.CartRepository;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartCacheServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartMapper cartMapper;

    @InjectMocks
    private CartCacheServiceImpl cartCacheService;

    @Test
    void getCart_whenCartExists_shouldReturnCart() {
        UUID userUuid = UUID.randomUUID();
        CartDao cartDao = new CartDao();
        Cart expectedCart = new Cart();

        when(cartRepository.findByUserUuid(userUuid)).thenReturn(Mono.just(cartDao));
        when(cartMapper.cartDaoToCart(cartDao)).thenReturn(expectedCart);

        Mono<Cart> result = cartCacheService.getCart(userUuid);

        StepVerifier.create(result)
                .expectNext(expectedCart)
                .verifyComplete();

        verify(cartRepository).findByUserUuid(userUuid);
        verify(cartMapper).cartDaoToCart(cartDao);
    }

    @Test
    void getCart_whenCartNotExists_shouldThrowCartNotFoundException() {
        UUID userUuid = UUID.randomUUID();

        when(cartRepository.findByUserUuid(userUuid)).thenReturn(Mono.empty());

        Mono<Cart> result = cartCacheService.getCart(userUuid);

        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof CartNotFoundException &&
                        throwable.getMessage().equals("Корзина пользователя с UUID = " + userUuid + " не найдена"))
                .verify();

        verify(cartRepository).findByUserUuid(userUuid);
    }

    @Test
    void evict_shouldDoNothing() {
        UUID userUuid = UUID.randomUUID();

        cartCacheService.evict(userUuid).subscribe();
    }
}