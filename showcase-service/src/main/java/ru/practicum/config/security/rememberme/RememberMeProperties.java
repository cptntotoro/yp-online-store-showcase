package ru.practicum.config.security.rememberme;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Конфигурация Remember Me токенов
 *
 * @param key Ключ
 * @param tokenValiditySeconds Срок действия токена (в секундах)
 */
@ConfigurationProperties(prefix = "security.remember-me")
public record RememberMeProperties(String key, int tokenValiditySeconds) {
}
