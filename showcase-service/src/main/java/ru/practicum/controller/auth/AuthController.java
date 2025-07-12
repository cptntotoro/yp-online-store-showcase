package ru.practicum.controller.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.dto.auth.UserAuthDto;
import ru.practicum.exception.auth.UserAlreadyExistsException;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.service.user.UserService;

@Controller
@RequiredArgsConstructor
public class AuthController {
    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    @GetMapping("/login")
    public Mono<String> login(ServerWebExchange exchange, Model model) {
        if (exchange.getRequest().getQueryParams().get("error") != null) {
            model.addAttribute("hasError", true);
        }
        if (exchange.getRequest().getQueryParams().get("logout") != null) {
            model.addAttribute("hasLogout", true);
        }

        return Mono.just("auth/login");
    }

    @GetMapping("/sign-up")
    public Mono<String> showRegistrationForm(Model model) {
        return Mono.just(model.addAttribute("user", new UserAuthDto()))
                .thenReturn("auth/sign-up");
    }

    @PostMapping("/sign-up")
    public Mono<String> registerUser(@ModelAttribute("user") UserAuthDto userAuthDto, Model model) {
        return userService.register(userMapper.userAuthDtoToUser(userAuthDto))
                .then(Mono.just("redirect:/login"))
                .onErrorResume(UserAlreadyExistsException.class, e -> {
                    model.addAttribute("error", e.getMessage());
                    model.addAttribute("user", userAuthDto);
                    return Mono.just("auth/sign-up");
                });
    }
}
