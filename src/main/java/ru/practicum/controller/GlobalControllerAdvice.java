package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;
import ru.practicum.config.WebAttributes;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.service.cart.CartService;

import java.util.UUID;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    /**
     * Сервис управления корзиной товаров
     */
    private final CartService cartService;

    /**
     * Маппер корзины товаров
     */
    private final CartMapper cartMapper;

    @ModelAttribute
    public void addCommonAttributes(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid,
                                    Model model) {
        model.addAttribute("cart", cartMapper.cartToCartDto(cartService.getCachedCart(userUuid)));
    }
}
