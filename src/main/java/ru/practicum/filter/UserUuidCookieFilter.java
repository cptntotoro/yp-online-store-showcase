package ru.practicum.filter;

import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import ru.practicum.config.WebAttributes;
import ru.practicum.service.user.UserService;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Реактивный WebFilter, добавляющий UUID пользователя в куку и контекст
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class UserUuidCookieFilter implements WebFilter {
    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        List<HttpCookie> cookies = exchange.getRequest().getCookies().getOrDefault(WebAttributes.USER_UUID, List.of());

        UUID existingUuid = null;
        if (!cookies.isEmpty()) {
            try {
                existingUuid = UUID.fromString(cookies.getFirst().getValue());
            } catch (IllegalArgumentException ignored) {
            }
        }

        UUID finalExistingUuid = existingUuid;

        return Mono.defer(() -> {
            if (finalExistingUuid == null) {
                return createNewUserAndSetCookie(exchange, chain);
            }

            return userService.existsByUuid(finalExistingUuid)
                    .flatMap(exists -> {
                        if (exists) {
                            exchange.getAttributes().put(WebAttributes.USER_UUID, finalExistingUuid);
                            return chain.filter(exchange);
                        } else {
                            return createNewUserAndSetCookie(exchange, chain);
                        }
                    })
                    .onErrorResume(e -> createNewUserAndSetCookie(exchange, chain));
        });
    }

    private Mono<Void> createNewUserAndSetCookie(ServerWebExchange exchange, WebFilterChain chain) {
        return userService.add()
                .map(user -> {
                    UUID newUuid = user.getUuid();

                    ResponseCookie cookie = ResponseCookie.from(WebAttributes.USER_UUID, newUuid.toString())
                            .path("/")
                            .maxAge(Duration.ofDays(30))
                            .httpOnly(true)
                            .sameSite("Lax")
                            .secure(false)
                            .build();

                    exchange.getResponse().addCookie(cookie);
                    exchange.getAttributes().put(WebAttributes.USER_UUID, newUuid);
                    return exchange;
                })
                .flatMap(chain::filter);
    }
}
