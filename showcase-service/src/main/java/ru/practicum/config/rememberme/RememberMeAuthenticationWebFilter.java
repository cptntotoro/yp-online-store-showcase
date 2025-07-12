package ru.practicum.config.rememberme;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.server.authentication.AnonymousAuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Фильтр для проверки наличия куки
 */
public class RememberMeAuthenticationWebFilter implements WebFilter {
    private final ServerAuthenticationConverter converter;
    private final AnonymousAuthenticationWebFilter anonymousFilter;

    public RememberMeAuthenticationWebFilter(@NonNull ServerAuthenticationConverter converter,
                                             @NonNull AnonymousAuthenticationWebFilter anonymousFilter) {
        this.converter = converter;
        this.anonymousFilter = anonymousFilter;
    }

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        return Mono.defer(() -> converter.convert(exchange))
                .flatMap(authentication -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication)))
                .onErrorResume(e -> {
                    // Удаляем куку при ошибке и продолжаем как анонимный пользователь
                    if (e instanceof UsernameNotFoundException || e instanceof BadCredentialsException) {
                        ((RememberMeAuthenticationConverter) converter).removeRememberMeCookie(exchange);
                    }
                    return anonymousFilter.filter(exchange, chain);
                })
                .switchIfEmpty(Mono.defer(() -> anonymousFilter.filter(exchange, chain)));
    }
}
