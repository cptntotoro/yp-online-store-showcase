package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;
import reactor.core.publisher.Mono;
import ru.practicum.config.WebAttributes;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.service.cart.CartService;

import java.util.UUID;

@ControllerAdvice(basePackages = {
        "ru.practicum.controller.cart",
        "ru.practicum.controller.order",
        "ru.practicum.controller.payment",
        "ru.practicum.controller.product"
})
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
    public Mono<Void> addCommonAttributes(@RequestAttribute(name = WebAttributes.USER_UUID, required = false) UUID userUuid,
                                          Model model) {

        if (userUuid == null) {
            return Mono.empty(); // Пропускаем для анонимных пользователей
        }

        return cartService.get(userUuid)
                .map(cartMapper::cartToCartDto)
                .doOnNext(cartDto -> model.addAttribute("cart", cartDto))
                .then();
    }
}
