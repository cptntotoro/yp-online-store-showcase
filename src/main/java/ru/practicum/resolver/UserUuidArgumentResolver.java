package ru.practicum.resolver;

import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import ru.practicum.annotation.CurrentUserUuid;
import ru.practicum.service.user.UserService;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserUuidArgumentResolver implements HandlerMethodArgumentResolver {

    private final UserService userService;

    private static final String USER_UUID_COOKIE_NAME = "userUuid";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUserUuid.class) &&
                parameter.getParameterType().equals(UUID.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        return webRequest.getAttribute(USER_UUID_COOKIE_NAME, NativeWebRequest.SCOPE_REQUEST);
    }
}