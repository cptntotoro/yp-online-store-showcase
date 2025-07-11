package ru.practicum.controller;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.service.cart.CartService;

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

//    @Test
//    void addCommonAttributes_ShouldAddCartToModel() {
//        UUID userId = UUID.randomUUID();
//        Cart cart = new Cart();
//        CartDto cartDto = new CartDto();
//
//        Mono<Cart> cartMono = Mono.just(cart);
//        when(cartService.get(userId)).thenReturn(cartMono);
//        when(cartMapper.cartToCartDto(cart)).thenReturn(cartDto);
//
//        Mono<Void> result = globalControllerAdvice.addCommonAttributes(userId, model);
//
//        StepVerifier.create(result)
//                .verifyComplete();
//
//        verify(cartService).get(userId);
//        verify(cartMapper).cartToCartDto(cart);
//        verify(model).addAttribute("cart", cartDto);
//    }
}