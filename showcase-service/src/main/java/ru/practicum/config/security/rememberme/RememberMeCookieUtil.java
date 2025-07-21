package ru.practicum.config.security.rememberme;

import org.springframework.http.ResponseCookie;

/**
 * Утилитный класс для работы с куками аутентификации Remember Me
 */
public class RememberMeCookieUtil {
    /**
     * Название куки
     */
    public static final String COOKIE_NAME = "remember-me";

    /**
     * Путь действия куки
     */
    private static final String COOKIE_PATH = "/";

    // TODO:
    /**
     * Политика SameSite для куки (по умолчанию Lax)
     */
    private static final String SAME_SITE = "Lax";

    /**
     * Создать Remember Me куку
     *
     * @param token Токен аутентификации в зашифрованном виде
     * @param maxAgeSeconds Время жизни куки в секундах
     * @return {@link ResponseCookie}
     */
    public static ResponseCookie createRememberMeCookie(String token, int maxAgeSeconds) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .maxAge(maxAgeSeconds)
                .path(COOKIE_PATH)
                .httpOnly(true)
                .sameSite(SAME_SITE)
                .build();
    }

    /**
     * Удалить Remember Me куку
     */
    public static ResponseCookie clearRememberMeCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .maxAge(0)
                .path(COOKIE_PATH)
                .httpOnly(true)
                .sameSite(SAME_SITE)
                .build();
    }
}
