package ru.practicum.config.security.rememberme;

import org.springframework.http.server.PathContainer;
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
import ru.practicum.config.security.PermittedPaths;

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
        if (isPermittedPath(exchange.getRequest().getPath().toString())) {
            return chain.filter(exchange);
        }

        return exchange.getPrincipal()
                .flatMap(principal -> chain.filter(exchange))
                .switchIfEmpty(processRememberMe(exchange, chain));
    }

    private Mono<Void> processRememberMe(ServerWebExchange exchange, WebFilterChain chain) {
        return converter.convert(exchange)
                .flatMap(auth -> chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth)))
                .onErrorResume(e -> {
                    if (!exchange.getResponse().isCommitted() &&
                            (e instanceof UsernameNotFoundException || e instanceof BadCredentialsException)) {
                        ((RememberMeAuthenticationConverter) converter).removeRememberMeCookie(exchange);
                        return anonymousFilter.filter(exchange, chain);
                    }
                    return Mono.error(e);
                });
    }

    private boolean isPermittedPath(String path) {
        PathContainer container = PathContainer.parsePath(path);
        return PermittedPaths.PERMITTED_PATH_PATTERNS.stream()
                .anyMatch(p -> p.matches(container));
    }
}