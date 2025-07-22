package ru.practicum.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import reactor.core.publisher.Mono;
import ru.practicum.dto.cart.CartDto;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.user.User;
import ru.practicum.service.cart.CartService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalControllerAdviceTest {

    @Mock
    private CartService cartService;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private Model model;

    @Mock
    private User user;

    @InjectMocks
    private GlobalControllerAdvice globalControllerAdvice;

    @Test
    void addCommonAttributes_WhenUserAuthenticated_ShouldAddAttributes() {
        UUID userId = UUID.randomUUID();
        Cart cart = new Cart();
        CartDto cartDto = new CartDto();

        when(user.getUuid()).thenReturn(userId);
        when(cartService.get(userId)).thenReturn(Mono.just(cart));
        when(cartMapper.cartToCartDto(cart)).thenReturn(cartDto);

        globalControllerAdvice.addCommonAttributes(user, model).block();

        verify(model).addAttribute(eq("isAuthenticated"), eq(true));
        verify(model).addAttribute(eq("cart"), eq(cartDto));
    }

    @Test
    void addCommonAttributes_WhenUserNotAuthenticated_ShouldOnlyAddIsAuthenticated() {
        User nullUser = null;

        globalControllerAdvice.addCommonAttributes(nullUser, model).block();

        verify(model).addAttribute(eq("isAuthenticated"), eq(false));
        verify(cartService, never()).get(any());
        verify(cartMapper, never()).cartToCartDto(any());
    }
}