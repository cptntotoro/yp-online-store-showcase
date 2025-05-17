package ru.practicum.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.practicum.interceptor.UserUuidInterceptor;
import ru.practicum.resolver.UserUuidArgumentResolver;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final UserUuidInterceptor userUuidInterceptor;
    private final UserUuidArgumentResolver userUuidArgumentResolver;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userUuidInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/error", "/static/**");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userUuidArgumentResolver);
    }
}