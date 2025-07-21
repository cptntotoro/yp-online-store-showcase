package ru.practicum.config.security;

import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;

public class PermittedPaths {
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
            "/fragments/**"
    );

    public static final List<PathPattern> PERMITTED_PATH_PATTERNS = PATTERNS.stream()
            .map(pattern -> new PathPatternParser().parse(pattern))
            .toList();
}
