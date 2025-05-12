package ru.practicum.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.practicum.config.WebAttributes;
import ru.practicum.service.user.UserService;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

/**
 * Фильтр куки для идентификатора пользователя
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class UserUuidCookieFilter extends OncePerRequestFilter {
    /**
     * Сервис управления пользователями
     */
    private final UserService userService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws IOException, ServletException {

        // Проверяем наличие куки
        UUID userUuid = extractUuidFromRequest(request);
        boolean needNewCookie = false;

        // Если куки нет - создаем нового пользователя и устанавливаем куку
        if (userUuid == null) {
                userUuid = userService.add().getUuid();
                setUserUuidCookie(response, userUuid);
        } else {
            // Проверяем существование пользователя в базе
            try {
                if (!userService.existsByUuid(userUuid)) {
                    needNewCookie = true;
                }
            } catch (Exception e) {
                // В случае ошибки создать нового пользователя
                needNewCookie = true;
            }

            if (needNewCookie) {
                userUuid = userService.add().getUuid();
                setUserUuidCookie(response, userUuid);
            }

        }

        // Добавляем USER_UUID в атрибуты запроса для использования в контроллерах
        request.setAttribute(WebAttributes.USER_UUID, userUuid);

        chain.doFilter(request, response);
    }

    private UUID extractUuidFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if (WebAttributes.USER_UUID.equals(cookie.getName())) {
                try {
                    return UUID.fromString(cookie.getValue());
                } catch (IllegalArgumentException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private void setUserUuidCookie(HttpServletResponse response, UUID userUuid) {
        ResponseCookie cookie = ResponseCookie.from(WebAttributes.USER_UUID, userUuid.toString())
                .path("/")
                .maxAge(Duration.ofDays(30))
                .httpOnly(true)
                .sameSite("Lax")
                .secure(false)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
