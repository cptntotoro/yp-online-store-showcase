package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestAttribute;
import ru.practicum.config.WebAttributes;
import ru.practicum.service.cart.CartService;

import java.util.UUID;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CartService cartService;

    @ModelAttribute
    public void addCommonAttributes(@RequestAttribute(WebAttributes.USER_UUID) UUID userUuid,
                                    Model model) {
//        model.addAttribute("cart", cartService.get(userUuid));
        model.addAttribute("cartTotal", cartService.getCachedCartTotal(userUuid));
    }
}
