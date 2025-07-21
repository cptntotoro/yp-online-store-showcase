package ru.practicum.config.security.rememberme;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Сервис управления Remember Me токенами
 */
@Component
@RequiredArgsConstructor
public class RememberMeTokenService {

    /**
     * Свойства Remember Me
     */
    private final RememberMeProperties rememberMeProperties;

    /**
     * Разделитель частей токена
     */
    private static final String TOKEN_DELIMITER = ":";

    /**
     * Сгенерировать токен
     *
     * @param username Имя пользователя (username)
     * @param expiryTime Временная метка окончания действия токена (в миллисекундах)
     * @param password Текущий пароль пользователя (или его хеш)
     * @return Хешированный токен в Base64-формате
     */
    public String generateToken(String username, long expiryTime, String password) {
        String data = String.join(TOKEN_DELIMITER, username, String.valueOf(expiryTime), password, rememberMeProperties.key());
        return Base64.getEncoder().encodeToString(sha256(data));
    }

    /**
     * Вычислить подпись токена. Фактически делегирует вызов в {@link #generateToken}
     *
     * @param username   Имя пользователя
     * @param expiryTime Время окончания действия токена
     * @param password   Пароль пользователя
     * @return Подпись токена
     */
    public String calculateSignature(String username, long expiryTime, String password) {
        return generateToken(username, expiryTime, password);
    }

    /**
     * Декодировать remember-me куки
     *
     * @param cookieValue Значение куки
     * @return Массив строк: [username, expiryTime, signature]
     * @throws IllegalArgumentException Если кука пуста, некорректно отформатирована или не может быть декодирована
     */
    public String[] decodeCookie(String cookieValue) {
        try {
            if (cookieValue == null || cookieValue.isEmpty()) {
                throw new IllegalArgumentException("Кука Remember Me пуста");
            }
            String decoded = new String(Base64.getDecoder().decode(cookieValue), StandardCharsets.UTF_8);
            String[] tokens = decoded.split(TOKEN_DELIMITER);
            if (tokens.length != 3) {
                throw new IllegalArgumentException("Некорректный формат Remember Me токена");
            }
            return tokens;
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка дешифрования Remember Me куки", e);
        }
    }

    /**
     * Хеширует строку алгоритмом SHA-256.
     *
     * @param data Строка для хеширования
     * @return Массив байтов хеша
     * @throws IllegalStateException Если SHA-256 недоступен в JVM
     */
    private byte[] sha256(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 недоступен", e);
        }
    }

    /**
     * Сравнивает две строки в постоянное время, чтобы избежать атак по времени исполнения (timing attacks).
     *
     * @param a Первая строка
     * @param b Вторая строка
     * @return true, если строки идентичны; false — в остальных случаях
     */
    public boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}