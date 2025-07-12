package ru.practicum.config.rememberme;

import org.springframework.web.server.ServerWebExchange;

/**
 * Менеджер куки remember me
 */
public interface RememberMeCookieManager {
    void removeRememberMeCookie(ServerWebExchange exchange);
}
