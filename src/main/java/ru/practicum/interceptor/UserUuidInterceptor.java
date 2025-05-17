package ru.practicum.interceptor;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ru.practicum.model.user.User;
import ru.practicum.service.user.UserService;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class UserUuidInterceptor implements HandlerInterceptor {

    private final UserService userService;
    private static final String USER_UUID_COOKIE = "USER_UUID";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Получаем userUuid из куки
        String userUuid = getCookieValue(request, USER_UUID_COOKIE);

        // Если нет куки - создаем нового пользователя и получаем его UUID
        if (userUuid == null || userUuid.isEmpty()) {
            User newUser = userService.add();
            userUuid = newUser.getUuid().toString();
            setCookie(response, userUuid);
        }

        request.setAttribute("userUuid", userUuid);
        return true;
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;

        return Arrays.stream(request.getCookies())
                .filter(cookie -> name.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }

    private void setCookie(HttpServletResponse response, String value) {
        Cookie cookie = new Cookie(USER_UUID_COOKIE, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 дней
        response.addCookie(cookie);
    }
}