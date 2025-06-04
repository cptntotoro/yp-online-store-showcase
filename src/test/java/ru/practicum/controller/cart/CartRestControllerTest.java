package ru.practicum.controller.cart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.model.cart.Cart;
import ru.practicum.service.cart.CartServiceImpl;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartRestControllerTest {

    @Mock
    private CartServiceImpl cartService;

    @InjectMocks
    private CartRestController cartRestController;

    @Test
    void addToCart_ShouldReturnTotalPrice() {
        UUID userUuid = UUID.randomUUID();
        UUID productUuid = UUID.randomUUID();
        int quantity = 2;
        BigDecimal expectedTotal = new BigDecimal("100.50");
        Cart mockCart = new Cart();
        mockCart.setTotalPrice(expectedTotal);

        when(cartService.addToCart(userUuid, productUuid, quantity)).thenReturn(mockCart);

        BigDecimal result = cartRestController.addToCart(userUuid, productUuid, quantity);

        assertEquals(expectedTotal, result);
        verify(cartService).addToCart(userUuid, productUuid, quantity);
    }

    @Test
    void updateCartItem_ShouldReturnUpdatedTotalPrice() {
        UUID userUuid = UUID.randomUUID();
        UUID productUuid = UUID.randomUUID();
        int newQuantity = 3;
        BigDecimal expectedTotal = new BigDecimal("150.75");
        Cart mockCart = new Cart();
        mockCart.setTotalPrice(expectedTotal);

        when(cartService.getCachedCart(userUuid)).thenReturn(mockCart);

        BigDecimal result = cartRestController.updateCartItem(productUuid, newQuantity, userUuid);

        assertEquals(expectedTotal, result);
        verify(cartService).updateQuantity(userUuid, productUuid, newQuantity);
        verify(cartService).getCachedCart(userUuid);
    }

    @Test
    void removeFromCart_ShouldReturnUpdatedTotalPrice() {
        UUID userUuid = UUID.randomUUID();
        UUID productUuid = UUID.randomUUID();
        BigDecimal expectedTotal = new BigDecimal("50.25");
        Cart mockCart = new Cart();
        mockCart.setTotalPrice(expectedTotal);

        when(cartService.getCachedCart(userUuid)).thenReturn(mockCart);

        BigDecimal result = cartRestController.removeFromCart(productUuid, userUuid);

        assertEquals(expectedTotal, result);
        verify(cartService).removeFromCart(userUuid, productUuid);
        verify(cartService).getCachedCart(userUuid);
    }
}