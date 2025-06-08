package ru.practicum.controller.cart;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
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

        when(cartService.addToCart(userUuid, productUuid, quantity)).thenReturn(Mono.just(mockCart));

        BigDecimal result = cartRestController.addToCart(userUuid, productUuid, quantity).block();

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

        when(cartService.updateQuantity(userUuid, productUuid, newQuantity)).thenReturn(Mono.empty());
        when(cartService.get(userUuid)).thenReturn(Mono.just(mockCart));

        BigDecimal result = cartRestController.updateCartItem(productUuid, newQuantity, userUuid).block();

        assertEquals(expectedTotal, result);
        verify(cartService).updateQuantity(userUuid, productUuid, newQuantity);
        verify(cartService).get(userUuid);
    }

    @Test
    void removeFromCart_ShouldReturnUpdatedTotalPrice() {
        UUID userUuid = UUID.randomUUID();
        UUID productUuid = UUID.randomUUID();
        BigDecimal expectedTotal = new BigDecimal("50.25");
        Cart mockCart = new Cart();
        mockCart.setTotalPrice(expectedTotal);

        when(cartService.removeFromCart(userUuid, productUuid)).thenReturn(Mono.empty());
        when(cartService.get(userUuid)).thenReturn(Mono.just(mockCart));

        BigDecimal result = cartRestController.removeFromCart(productUuid, userUuid).block();

        assertEquals(expectedTotal, result);
        verify(cartService).removeFromCart(userUuid, productUuid);
        verify(cartService).get(userUuid);
    }
}