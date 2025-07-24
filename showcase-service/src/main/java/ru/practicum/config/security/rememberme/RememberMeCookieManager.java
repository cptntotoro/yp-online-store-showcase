package ru.practicum.config.security.rememberme;

import org.springframework.web.server.ServerWebExchange;

/**
 * Менеджер куки remember me
 */
public interface RememberMeCookieManager {

    /**
     * Удалить куку remember me
     *
     * @param exchange ServerWebExchange
     */
    void removeRememberMeCookie(ServerWebExchange exchange);
}
