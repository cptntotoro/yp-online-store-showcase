package ru.practicum.config.security.rememberme;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.service.auth.UserDetailsServiceImpl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Извлекает токен из куки
 */
@AllArgsConstructor
public class RememberMeAuthenticationConverter implements ServerAuthenticationConverter, RememberMeCookieManager {

    /**
     * Кастомный {@link UserDetailsServiceImpl}
     */
    private final ReactiveUserDetailsService userDetailsService;

    /**
     * Remember me ключ
     */
    private final String rememberMeKey;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.defer(() ->
                exchange.getPrincipal()
                        .ofType(Authentication.class)
                        .switchIfEmpty(Mono.defer(() ->
                                Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst("remember-me"))
                                        .flatMap(cookie -> {
                                            try {
                                                String[] cookieTokens = decodeCookie(cookie.getValue());
                                                return validateTokenAndGetUser(cookieTokens)
                                                        .map(userDetails -> new UsernamePasswordAuthenticationToken(
                                                                userDetails,
                                                                userDetails.getPassword(),
                                                                userDetails.getAuthorities()
                                                        ))
                                                        .doOnError(e -> removeRememberMeCookie(exchange));
                                            } catch (Exception e) {
                                                removeRememberMeCookie(exchange);
                                                return Mono.error(e);
                                            }
                                        })
                        )));
    }

    @Override
    public void removeRememberMeCookie(ServerWebExchange exchange) {
        ResponseCookie cookie = ResponseCookie.from("remember-me", "")
                .maxAge(0)
                .path("/")
                .httpOnly(true)
                .sameSite("Lax")
                .build();
        exchange.getResponse().addCookie(cookie);
    }

    /**
     * Проверить токен [username, expiryTime, signature] и получить пользователя
     *
     * @param cookieTokens Куки токены
     * @return Пользователь
     */
    private Mono<UserDetails> validateTokenAndGetUser(String[] cookieTokens) {
        if (cookieTokens == null || cookieTokens.length != 3) {
            return Mono.error(new BadCredentialsException("Invalid remember-me token format"));
        }

        String username = cookieTokens[0];
        long expiryTime;

        try {
            expiryTime = Long.parseLong(cookieTokens[1]);
        } catch (NumberFormatException e) {
            return Mono.error(new BadCredentialsException("Invalid expiry time in remember-me token"));
        }

        String expectedSignature = cookieTokens[2];

        return userDetailsService.findByUsername(username)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new UsernameNotFoundException("User not found"))
                ))
                .flatMap(userDetails -> {
                    if (System.currentTimeMillis() > expiryTime) {
                        return Mono.error(new CredentialsExpiredException("Remember-me token has expired"));
                    }

                    String calculatedSignature = calculateSignature(username, expiryTime, userDetails.getPassword());
                    if (!constantTimeEquals(calculatedSignature, expectedSignature)) {
                        return Mono.error(new BadCredentialsException("Invalid remember-me token signature"));
                    }

                    if (!userDetails.isEnabled()) {
                        return Mono.error(new DisabledException("User account is disabled"));
                    }

                    if (!userDetails.isAccountNonLocked()) {
                        return Mono.error(new LockedException("User account is locked"));
                    }

                    if (!userDetails.isAccountNonExpired()) {
                        return Mono.error(new AccountExpiredException("User account has expired"));
                    }

                    if (!userDetails.isCredentialsNonExpired()) {
                        return Mono.error(new CredentialsExpiredException("User credentials have expired"));
                    }

                    return Mono.just(userDetails);
                })
                .onErrorResume(e -> {
                    System.err.println(e.getMessage());
                    if (e instanceof AuthenticationException) {
                        return Mono.error(e);
                    }
                    return Mono.error(new BadCredentialsException("Invalid remember-me token", e));
                });
    }

    /**
     * Создать подпись
     *
     * @param username Имя пользователя (username)
     * @param expiryTime Срок действия
     * @param password Пароль
     * @return SHA-256 подпись
     */
    private String calculateSignature(String username, long expiryTime, String password) {
        // Создаем подпись на основе username, времени и пароля
        String data = username + ":" + expiryTime + ":" + password + ":" + rememberMeKey;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No SHA-256 algorithm available", e);
        }
    }

    // Безопасное сравнение строк для предотвращения timing-атак
    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }

        int length = a.length();
        if (length != b.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < length; i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * Декодировать Base64 токен
     *
     * @param cookieValue Куки
     * @return username:expiryTime:signature
     */
    private String[] decodeCookie(String cookieValue) {
        try {
            // Делаем базовую проверку и декодирование
            if (cookieValue == null || cookieValue.isEmpty()) {
                throw new IllegalArgumentException("Cookie value is empty");
            }

            // Декодируем из Base64
            String decodedValue = new String(Base64.getDecoder().decode(cookieValue), StandardCharsets.UTF_8);

            // Разбиваем на компоненты: username:expiryTime:signature
            String[] tokens = decodedValue.split(":");
            if (tokens.length != 3) {
                throw new IllegalArgumentException("Invalid cookie format");
            }

            return tokens;
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid remember-me cookie", e);
        }
    }
}