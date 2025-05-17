//package ru.practicum.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.CookieValue;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import ru.practicum.service.cart.CartService;
//
//import java.math.BigDecimal;
//import java.util.UUID;
//
//@ControllerAdvice
//@RequiredArgsConstructor
//public class GlobalControllerAdvice {
//
//    private final CartService cartService;
//
//    @ModelAttribute
//    public void addCommonAttributes(@CookieValue(value = "userUuid") String userUuidCookie,
//                                    Model model) {
//        UUID userUuid = UUID.fromString(userUuidCookie);
//        BigDecimal cartTotal = cartService.getCachedCartTotal(userUuid);
//        model.addAttribute("cartTotal", cartTotal);
//    }
//}
