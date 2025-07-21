package ru.practicum.config.security;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;

/**
 * Публичные пути, не требующие авторизации
 */
public class PermittedPaths {

    private static final PathPatternParser patternParser = new PathPatternParser();

    /**
     * Паттерны для ServerHttpSecurity
     */
    public static final List<String> PATTERNS = List.of(
            "/favicon.ico",
            "/login",
            "/logout",
            "/notfound",
            "/error",
            "/sign-up",
            "/styles/**",
            "/scripts/**",
            "/images/**",
            "/templates/**",
            "/fragments/**",
            "/products/**"
    );

    /**
     * Проверить, требует ли путь авторизации
     *
     * @param path Путь
     * @return Да / Нет
     */
    public static boolean matches(String path) {
        PathContainer parsedPath = PathContainer.parsePath(path);
        return PERMITTED_PATH_PATTERNS.stream()
                .anyMatch(pattern -> pattern.matches(parsedPath));
    }

    private static final List<PathPattern> PERMITTED_PATH_PATTERNS = PATTERNS.stream()
            .map(patternParser::parse)
            .toList();

}