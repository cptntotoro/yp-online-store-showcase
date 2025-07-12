package ru.practicum.config.rememberme;

import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Создает токен при успешном входе
 */
public class RememberMeSuccessHandler implements ServerAuthenticationSuccessHandler {

    /**
     * Cекретный ключ
     */
    private final String rememberMeKey;
    /**
     * Время действия токена (в миллисекундах)
     */
    private final int tokenValiditySeconds;

    public RememberMeSuccessHandler(String rememberMeKey, int tokenValiditySeconds) {
        this.rememberMeKey = rememberMeKey;
        this.tokenValiditySeconds = tokenValiditySeconds;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange,
                                              Authentication authentication) {
        return webFilterExchange.getExchange().getFormData()
                .flatMap(formData -> {
                    if (formData.containsKey("remember-me")) {
                        String token = generateToken(authentication);
                        ResponseCookie cookie = ResponseCookie.from("remember-me", token)
                                .maxAge(tokenValiditySeconds)
                                .path("/")
                                .httpOnly(true)
                                .build();
                        webFilterExchange.getExchange().getResponse()
                                .addCookie(cookie);
                    }
                    return Mono.empty();
                });
    }

    /**
     * Сгенерировать токен
     *
     * @param authentication Аутентификация
     * @return Токен
     */
    private String generateToken(Authentication authentication) {
        System.err.println("Generating token for user: " + authentication.getName());
        String username = authentication.getName();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        long expiryTime = System.currentTimeMillis() + TimeUnit.DAYS.toMillis(14); // 2 недели

        String signature = calculateSignature(username, expiryTime, userDetails.getPassword());
        String tokenValue = username + ":" + expiryTime + ":" + signature;

        return Base64.getEncoder().encodeToString(tokenValue.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Генерация подписи при создании нового токена (при входе пользователя)
     *
     * @param username Имя пользователя (username)
     * @param expiryTime Время действия токена (в миллисекундах)
     * @param password Пароль пользователя
     * @return SHA-256 подпись
     */
    private String calculateSignature(String username, long expiryTime, String password) {
        try {
            // Собираем данные для подписи
            String data = String.join(":",
                    username,
                    String.valueOf(expiryTime),
                    password,
                    rememberMeKey
            );

            // Создаем экземпляр SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Вычисляем хеш
            byte[] hashBytes = digest.digest(
                    data.getBytes(StandardCharsets.UTF_8)
            );

            // Преобразуем бинарный хеш в строку для хранения в cookie
            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}