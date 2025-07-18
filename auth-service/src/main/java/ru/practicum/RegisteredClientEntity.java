package ru.practicum;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Table("oauth2_registered_client")
public class RegisteredClientEntity {
    @Id
    private String id;
    private String clientId;
    private Instant clientIdIssuedAt;
    private String clientSecret;
    private Instant clientSecretExpiresAt;
    private String clientName;
    private Set<String> clientAuthenticationMethods;
    private Set<String> authorizationGrantTypes;
    @Transient
    private Set<String> redirectUris;
    private String redirectUrisJson;
    private Set<String> scopes;
    private String clientSettings;
    private String tokenSettings;

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);

    public static RegisteredClientEntity fromRegisteredClient(RegisteredClient registeredClient) {
        RegisteredClientEntity entity = new RegisteredClientEntity();
        entity.setId(registeredClient.getId());
        entity.setClientId(registeredClient.getClientId());
        entity.setClientIdIssuedAt(Optional.ofNullable(registeredClient.getClientIdIssuedAt()).orElse(Instant.now()));
        entity.setClientSecret(registeredClient.getClientSecret());
        entity.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
        entity.setClientName(registeredClient.getClientName());
        entity.setClientAuthenticationMethods(registeredClient.getClientAuthenticationMethods().stream()
                .map(ClientAuthenticationMethod::getValue)
                .collect(Collectors.toSet()));
        entity.setAuthorizationGrantTypes(registeredClient.getAuthorizationGrantTypes().stream()
                .map(AuthorizationGrantType::getValue)
                .collect(Collectors.toSet()));
        entity.setScopes(registeredClient.getScopes());
        entity.setClientSettings(serializeSettings(registeredClient.getClientSettings().getSettings()));
        entity.setTokenSettings(serializeSettings(registeredClient.getTokenSettings().getSettings()));

        try {
            Set<String> uris = registeredClient.getRedirectUris();
            entity.setRedirectUris(uris != null ? uris : Set.of());
            entity.setRedirectUrisJson(uris != null && !uris.isEmpty() ?
                    objectMapper.writeValueAsString(new ArrayList<>(uris)) : "[]");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize redirect URIs", e);
        }

        return entity;
    }

    public RegisteredClient toRegisteredClient() {
        RegisteredClient.Builder builder = RegisteredClient.withId(this.id)
                .clientId(this.clientId)
                .clientIdIssuedAt(this.clientIdIssuedAt)
                .clientSecret(this.clientSecret)
                .clientSecretExpiresAt(this.clientSecretExpiresAt)
                .clientName(this.clientName)
                .clientAuthenticationMethods(methods ->
                        this.clientAuthenticationMethods.stream()
                                .map(method -> {
                                    // Удаляем возможные фигурные скобки и кавычки
                                    String cleanMethod = method.replaceAll("[{}\"]", "").trim();
                                    return new ClientAuthenticationMethod(cleanMethod);
                                })
                                .forEach(methods::add))
                .authorizationGrantTypes(grants ->
                        this.authorizationGrantTypes.stream()
                                .map(grantType -> {
                                    String cleanGrantType = grantType.replaceAll("[{}\"]", "").trim();
                                    return new AuthorizationGrantType(cleanGrantType);
                                })
                                .forEach(grants::add))
                .redirectUris(uris -> Optional.ofNullable(this.redirectUris).ifPresent(uris::addAll))
                .scopes(scopes -> scopes.addAll(this.scopes.stream().map(scope -> scope.replaceAll("[{}\"]", "").trim()).collect(Collectors.toSet())))
                .clientSettings(deserializeClientSettings(this.clientSettings))
                .tokenSettings(deserializeTokenSettings(this.tokenSettings));

        try {
            if (this.redirectUrisJson != null && !this.redirectUrisJson.isEmpty()) {
                List<String> uris = objectMapper.readValue(this.redirectUrisJson, new TypeReference<List<String>>() {});
                if (uris != null && !uris.isEmpty()) {
                    builder.redirectUris(redirectUris -> redirectUris.addAll(uris));
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize redirect URIs", e);
        }

        return builder.build();
    }

    private static String serializeSettings(Object settings) {
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize settings", e);
        }
    }

    private static ClientSettings deserializeClientSettings(String json) {
        try {
            Map<String, Object> settings = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            return ClientSettings.withSettings(settings).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize client settings", e);
        }
    }

    private static TokenSettings deserializeTokenSettings(String json) {
        try {
            Map<String, Object> rawSettings = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> settings = new HashMap<>();

            // Обрабатываем каждое поле с учетом его типа
            for (Map.Entry<String, Object> entry : rawSettings.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                // Преобразуем значения в нужные типы
                if (key.endsWith("time-to-live")) {
                    // Для временных интервалов преобразуем в Duration
                    if (value instanceof Number) {
                        value = java.time.Duration.ofSeconds(((Number) value).longValue());
                    }
                } else if (key.equals("settings.token.access-token-format")) {
                    // Для формата токена преобразуем в соответствующий объект
                    if (value instanceof Map) {
                        Map<?, ?> formatMap = (Map<?, ?>) value;
                        value = new org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat(
                                String.valueOf(formatMap.get("value"))
                        );
                    }
                } else if (key.equals("settings.token.id-token-signature-algorithm")) {
                    // Для алгоритма подписи преобразуем в SignatureAlgorithm
                    if (value instanceof String) {
                        value = org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.from(
                                String.valueOf(value)
                        );
                    }
                }

                settings.put(key, value);
            }

            return TokenSettings.withSettings(settings).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize token settings", e);
        }
    }
}