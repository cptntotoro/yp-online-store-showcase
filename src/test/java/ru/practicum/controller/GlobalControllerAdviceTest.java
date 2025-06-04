package ru.practicum.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import ru.practicum.dto.cart.CartDto;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.service.cart.CartService;

import java.util.UUID;

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

    @InjectMocks
    private GlobalControllerAdvice globalControllerAdvice;

    @Test
    void addCommonAttributes_ShouldAddCartToModel() {
        UUID userUuid = UUID.randomUUID();
        Cart expectedCart = new Cart();
        CartDto expectedCartDto = new CartDto();

        when(cartService.getCachedCart(userUuid)).thenReturn(expectedCart);
        when(cartMapper.cartToCartDto(expectedCart)).thenReturn(expectedCartDto);

        globalControllerAdvice.addCommonAttributes(userUuid, model);

        verify(cartService).getCachedCart(userUuid);
        verify(cartMapper).cartToCartDto(expectedCart);
        verify(model).addAttribute("cart", expectedCartDto);
    }

    @Test
    void addCommonAttributes_ShouldWorkWithEmptyCart() {
        UUID userUuid = UUID.randomUUID();
        Cart emptyCart = new Cart();
        CartDto emptyCartDto = new CartDto();

        when(cartService.getCachedCart(userUuid)).thenReturn(emptyCart);
        when(cartMapper.cartToCartDto(emptyCart)).thenReturn(emptyCartDto);

        globalControllerAdvice.addCommonAttributes(userUuid, model);

        verify(model).addAttribute("cart", emptyCartDto);
    }
}
