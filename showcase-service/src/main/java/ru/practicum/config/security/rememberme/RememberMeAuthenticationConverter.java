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
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Извлекает токен из куки
 */
@Component
@AllArgsConstructor
public class RememberMeAuthenticationConverter implements ServerAuthenticationConverter, RememberMeCookieManager {

    private static final String COOKIE_NAME = "remember-me";

    private final ReactiveUserDetailsService userDetailsService;

    private final RememberMeTokenService tokenService;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return exchange.getPrincipal()
                .ofType(Authentication.class)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(COOKIE_NAME))
                                .flatMap(cookie -> {
                                    // TODO:
                                    System.err.println("REMEMBER-ME cookie found: " + cookie.getValue());
                                    try {
                                        String[] tokens = tokenService.decodeCookie(cookie.getValue());
                                        return validateToken(tokens)
                                                .map(user -> new UsernamePasswordAuthenticationToken(
                                                        user, user.getPassword(), user.getAuthorities()
                                                ));
                                    } catch (Exception e) {
                                        removeRememberMeCookie(exchange);
                                        return Mono.error(new BadCredentialsException("Неверный Remember-Me куки", e));
                                    }
                                })
                ));
    }

    private Mono<UserDetails> validateToken(String[] tokens) {
        if (tokens.length != 3) {
            return Mono.error(new BadCredentialsException("Неверный формат Remember-Me токена"));
        }

        String username = tokens[0];
        long expiryTime;

        try {
            expiryTime = Long.parseLong(tokens[1]);
        } catch (NumberFormatException e) {
            return Mono.error(new BadCredentialsException("Некорректное время действия Remember-Me токена"));
        }

        String expectedSignature = tokens[2];

        return userDetailsService.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь с username " + username + " не найден")))
                .flatMap(user -> {
                    // TODO:
                    System.err.println("REMEMBER-ME success for user: " + user.getUsername());
                    if (System.currentTimeMillis() > expiryTime) {
                        return Mono.error(new CredentialsExpiredException("Срок действия Remember-Me токена истек"));
                    }

                    String actualSignature = tokenService.calculateSignature(username, expiryTime, user.getPassword());
                    if (!tokenService.constantTimeEquals(expectedSignature, actualSignature)) {
                        return Mono.error(new BadCredentialsException("Некорректная подпись Remember-Me токена"));
                    }

                    if (!user.isEnabled()) return Mono.error(new DisabledException("Пользователь отключён"));
                    if (!user.isAccountNonLocked()) return Mono.error(new LockedException("Учетная запись заблокирована"));
                    if (!user.isAccountNonExpired()) return Mono.error(new AccountExpiredException("Срок действия учетной записи истёк"));
                    if (!user.isCredentialsNonExpired()) return Mono.error(new CredentialsExpiredException("Срок действия учетных данных истёк"));

                    return Mono.just(user);
                });
    }

    @Override
    public void removeRememberMeCookie(ServerWebExchange exchange) {
        ResponseCookie cookie = RememberMeCookieUtil.clearRememberMeCookie();
        exchange.getResponse().addCookie(cookie);
    }
}