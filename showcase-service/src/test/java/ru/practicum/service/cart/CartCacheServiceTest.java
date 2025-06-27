package ru.practicum.service.cart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.dao.cart.CartDao;
import ru.practicum.dao.cart.CartItemDao;
import ru.practicum.dto.cart.cache.CartCacheDto;
import ru.practicum.exception.cart.CartNotFoundException;
import ru.practicum.mapper.cart.CartItemMapper;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.cart.CartItem;
import ru.practicum.repository.cart.CartItemRepository;
import ru.practicum.repository.cart.CartRepository;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartCacheServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CartItemMapper cartItemMapper;

    @Mock
    private ReactiveRedisTemplate<String, CartCacheDto> cartCacheTemplate;

    @Mock
    private ReactiveValueOperations<String, CartCacheDto> valueOperations;

    @InjectMocks
    private CartCacheServiceImpl cartCacheService;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();

    @Test
    void getCart_WhenCached_ShouldReturnFromCache() {
        CartCacheDto cachedDto = new CartCacheDto();
        Cart expectedCart = new Cart();

        when(cartCacheTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("cart:" + userId)).thenReturn(Mono.just(cachedDto));
        when(cartMapper.fromCacheDto(cachedDto)).thenReturn(expectedCart);
        when(cartRepository.findByUserUuid(any())).thenReturn(Mono.empty());

        Mono<Cart> result = cartCacheService.getCart(userId);

        assertEquals(expectedCart, result.block());
    }

    @Test
    void getCart_WhenNotCached_ShouldFetchAndCache() {
        CartDao cartDao = new CartDao();
        cartDao.setUuid(cartId);
        CartItemDao cartItemDao = new CartItemDao();
        Cart cart = new Cart();
        cart.setUuid(cartId);
        CartItem cartItem = new CartItem();
        CartCacheDto dto = new CartCacheDto();

        when(cartCacheTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("cart:" + userId)).thenReturn(Mono.empty());
        when(cartRepository.findByUserUuid(userId)).thenReturn(Mono.just(cartDao));
        when(cartMapper.cartDaoToCart(cartDao)).thenReturn(cart);
        when(cartItemRepository.findByCartUuid(cartId)).thenReturn(Flux.just(cartItemDao));
        when(cartItemMapper.cartItemDaoToCartItem(cartItemDao)).thenReturn(cartItem);
        when(cartMapper.toCacheDto(cart)).thenReturn(dto);
        when(valueOperations.set(eq("cart:" + userId), eq(dto), any(Duration.class)))
                .thenReturn(Mono.just(true));

        Mono<Cart> result = cartCacheService.getCart(userId);

        Cart actualCart = result.block();
        assertNotNull(actualCart);
        assertEquals(1, actualCart.getItems().size());
        verify(valueOperations).set(eq("cart:" + userId), eq(dto), any(Duration.class));
    }

    @Test
    void getCart_WhenNotFound_ShouldThrowException() {
        when(cartCacheTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("cart:" + userId)).thenReturn(Mono.empty());
        when(cartRepository.findByUserUuid(userId)).thenReturn(Mono.empty());

        assertThrows(CartNotFoundException.class, () ->
                cartCacheService.getCart(userId).block());
    }

    @Test
    void evict_ShouldDeleteFromCache() {
        when(cartCacheTemplate.delete("cart:" + userId)).thenReturn(Mono.just(1L));

        Mono<Void> result = cartCacheService.evict(userId);

        assertNull(result.block());
    }

    @Test
    void cacheCart_WhenCartIsNull_ShouldDoNothing() {
        Mono<Void> result = cartCacheService.cacheCart(null);

        assertNull(result.block());
        verifyNoInteractions(cartCacheTemplate);
    }

    @Test
    void cacheCart_ShouldSaveToCache() {
        Cart cart = new Cart();
        cart.setUserUuid(userId);
        CartCacheDto dto = new CartCacheDto();
        dto.setUserUuid(userId);

        when(cartCacheTemplate.opsForValue()).thenReturn(valueOperations);
        when(cartMapper.toCacheDto(cart)).thenReturn(dto);
        when(valueOperations.set("cart:" + userId, dto, Duration.ofHours(1)))
                .thenReturn(Mono.just(true));

        Mono<Void> result = cartCacheService.cacheCart(cart);

        assertNull(result.block());
        verify(valueOperations).set("cart:" + userId, dto, Duration.ofHours(1));
    }
}