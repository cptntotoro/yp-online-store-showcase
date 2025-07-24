package ru.practicum.config.security.rememberme;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Создает токен при успешном входе
 */
@Component
@RequiredArgsConstructor
public class RememberMeSuccessHandler implements ServerAuthenticationSuccessHandler {

    /**
     * Сервис управления Remember Me токенами
     */
    private final RememberMeTokenService tokenService;

    /**
     * Конфигурация Remember Me токенов
     */
    private final RememberMeProperties rememberMeProperties;

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange exchange, Authentication authentication) {
        return exchange.getExchange().getFormData()
                .flatMap(formData -> {
                    if (formData.containsKey(RememberMeCookieUtil.COOKIE_NAME)) {
                        UserDetails user = (UserDetails) authentication.getPrincipal();
                        long expiryTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(rememberMeProperties.tokenValiditySeconds());
                        String token = encodeToken(authentication.getName(), expiryTime, user.getPassword());

                        ResponseCookie cookie = RememberMeCookieUtil.createRememberMeCookie(token, rememberMeProperties.tokenValiditySeconds());
                        exchange.getExchange().getResponse().addCookie(cookie);
                    }
                    return Mono.empty();
                });
    }

    private String encodeToken(String username, long expiryTime, String password) {
        String signature = tokenService.calculateSignature(username, expiryTime, password);
        String raw = String.join(":", username, String.valueOf(expiryTime), signature);
        return Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}