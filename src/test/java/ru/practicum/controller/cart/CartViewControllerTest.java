package ru.practicum.controller.cart;

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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartViewControllerTest {

    @Mock
    private CartService cartService;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private Model model;

    @InjectMocks
    private CartViewController cartViewController;

    @Test
    void showCart_ShouldReturnCartViewWithCartData() {
        UUID userUuid = UUID.randomUUID();
        Cart cart = new Cart();
        CartDto cartDto = new CartDto();

        when(cartService.get(userUuid)).thenReturn(cart);
        when(cartMapper.cartToCartDto(cart)).thenReturn(cartDto);

        String viewName = cartViewController.showCart(model, userUuid);

        assertEquals("cart/cart", viewName);
        verify(model).addAttribute("cart", cartDto);
        verify(cartService).get(userUuid);
        verify(cartMapper).cartToCartDto(cart);
    }

    @Test
    void removeFromCart_ShouldRemoveProductAndRedirect() {
        UUID userUuid = UUID.randomUUID();
        UUID productUuid = UUID.randomUUID();

        String redirectUrl = cartViewController.removeFromCart(userUuid, productUuid);

        assertEquals("redirect:/cart", redirectUrl);
        verify(cartService).removeFromCart(userUuid, productUuid);
    }

    @Test
    void clearCart_ShouldClearCartAndRedirect() {
        UUID userUuid = UUID.randomUUID();

        String redirectUrl = cartViewController.clearCart(userUuid);

        assertEquals("redirect:/cart", redirectUrl);
        verify(cartService).clear(userUuid);
    }

    @Test
    void showCart_WithEmptyCart_ShouldStillReturnView() {
        UUID userUuid = UUID.randomUUID();
        Cart emptyCart = new Cart();
        CartDto emptyCartDto = new CartDto();

        when(cartService.get(userUuid)).thenReturn(emptyCart);
        when(cartMapper.cartToCartDto(emptyCart)).thenReturn(emptyCartDto);

        String viewName = cartViewController.showCart(model, userUuid);

        assertEquals("cart/cart", viewName);
        verify(model).addAttribute("cart", emptyCartDto);
    }
}
