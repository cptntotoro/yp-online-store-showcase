package ru.practicum.config.security.rememberme;

import org.springframework.web.server.ServerWebExchange;

/**
 * Менеджер куки remember me
 */
public interface RememberMeCookieManager {
    void removeRememberMeCookie(ServerWebExchange exchange);
}
