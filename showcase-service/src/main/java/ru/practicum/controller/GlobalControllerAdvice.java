package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import reactor.core.publisher.Mono;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.user.User;
import ru.practicum.service.cart.CartService;

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
    public Mono<Void> addCommonAttributes(@AuthenticationPrincipal User user,
                                          Model model) {
        boolean isAuthenticated = user != null;

        model.addAttribute("isAuthenticated", isAuthenticated);

        if (!isAuthenticated) {
            return Mono.empty();
        }

        return cartService.get(user.getUuid())
                .map(cartMapper::cartToCartDto)
                .doOnNext(cartDto -> model.addAttribute("cart", cartDto))
                .then();
    }
}
