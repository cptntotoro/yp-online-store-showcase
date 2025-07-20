package ru.practicum.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;
import ru.practicum.dao.RegisteredClientDao;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RegisteredClientMapperImpl implements RegisteredClientMapper {
    private final ObjectMapper objectMapper = createConfiguredObjectMapper();

    private static ObjectMapper createConfiguredObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID);
    }

    @Override
    public RegisteredClientDao fromRegisteredClient(RegisteredClient registeredClient) {
        RegisteredClientDao entity = new RegisteredClientDao();
        mapBaseFields(registeredClient, entity);
        mapAuthenticationMethods(registeredClient, entity);
        mapGrantTypes(registeredClient, entity);
        mapScopesAndSettings(registeredClient, entity);
        mapRedirectUris(registeredClient, entity);
        return entity;
    }

    @Override
    public RegisteredClient toRegisteredClient(RegisteredClientDao dao) {
        RegisteredClient.Builder builder = createBaseBuilder(dao);
        configureAuthenticationMethods(builder, dao);
        configureGrantTypes(builder, dao);
        configureScopes(builder, dao);
        configureRedirectUris(builder, dao);
        applySettings(builder, dao);
        return builder.build();
    }

    private void mapBaseFields(RegisteredClient registeredClient, RegisteredClientDao dao) {
        dao.setId(registeredClient.getId());
        dao.setClientId(registeredClient.getClientId());
        dao.setClientIdIssuedAt(Optional.ofNullable(registeredClient.getClientIdIssuedAt()).orElse(Instant.now()));
        dao.setClientSecret(registeredClient.getClientSecret());
        dao.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
        dao.setClientName(registeredClient.getClientName());
    }

    private void mapAuthenticationMethods(RegisteredClient registeredClient, RegisteredClientDao dao) {
        dao.setClientAuthenticationMethods(
                registeredClient.getClientAuthenticationMethods().stream()
                        .map(ClientAuthenticationMethod::getValue)
                        .collect(Collectors.toSet())
        );
    }

    private void mapGrantTypes(RegisteredClient registeredClient, RegisteredClientDao dao) {
        dao.setAuthorizationGrantTypes(
                registeredClient.getAuthorizationGrantTypes().stream()
                        .map(AuthorizationGrantType::getValue)
                        .collect(Collectors.toSet())
        );
    }

    private void mapScopesAndSettings(RegisteredClient registeredClient, RegisteredClientDao dao) {
        dao.setScopes(registeredClient.getScopes());
        dao.setClientSettings(serializeSettings(registeredClient.getClientSettings().getSettings()));
        dao.setTokenSettings(serializeSettings(registeredClient.getTokenSettings().getSettings()));
    }

    private void mapRedirectUris(RegisteredClient registeredClient, RegisteredClientDao dao) {
        try {
            Set<String> uris = registeredClient.getRedirectUris();
            dao.setRedirectUris(uris != null ? uris : Set.of());
            dao.setRedirectUrisJson(uris != null && !uris.isEmpty() ?
                    objectMapper.writeValueAsString(new ArrayList<>(uris)) : "[]");
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка серивализации URI для редиректа", e);
        }
    }

    private RegisteredClient.Builder createBaseBuilder(RegisteredClientDao dao) {
        return RegisteredClient.withId(dao.getId())
                .clientId(dao.getClientId())
                .clientIdIssuedAt(dao.getClientIdIssuedAt())
                .clientSecret(dao.getClientSecret())
                .clientSecretExpiresAt(dao.getClientSecretExpiresAt())
                .clientName(dao.getClientName());
    }

    private void configureAuthenticationMethods(RegisteredClient.Builder builder, RegisteredClientDao dao) {
        builder.clientAuthenticationMethods(methods ->
                dao.getClientAuthenticationMethods().stream()
                        .map(this::cleanAuthMethod)
                        .map(ClientAuthenticationMethod::new)
                        .forEach(methods::add)
        );
    }

    private void configureGrantTypes(RegisteredClient.Builder builder, RegisteredClientDao dao) {
        builder.authorizationGrantTypes(grants ->
                dao.getAuthorizationGrantTypes().stream()
                        .map(this::cleanGrantType)
                        .map(AuthorizationGrantType::new)
                        .forEach(grants::add)
        );
    }

    private void configureScopes(RegisteredClient.Builder builder, RegisteredClientDao dao) {
        builder.scopes(scopes ->
                scopes.addAll(
                        dao.getScopes().stream()
                                .map(this::cleanScope)
                                .collect(Collectors.toSet())
                )
        );
    }

    private void configureRedirectUris(RegisteredClient.Builder builder, RegisteredClientDao dao) {
        Optional.ofNullable(dao.getRedirectUris()).ifPresent(uris -> uris.forEach(builder::redirectUri));

        try {
            if (dao.getRedirectUrisJson() != null && !dao.getRedirectUrisJson().isEmpty()) {
                List<String> uris = objectMapper.readValue(dao.getRedirectUrisJson(), new TypeReference<>() {});
                Optional.ofNullable(uris).ifPresent(list -> list.forEach(builder::redirectUri));
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка десериализации URI для редиректа", e);
        }
    }

    private void applySettings(RegisteredClient.Builder builder, RegisteredClientDao dao) {
        builder.clientSettings(deserializeClientSettings(dao.getClientSettings()));
        builder.tokenSettings(deserializeTokenSettings(dao.getTokenSettings()));
    }

    private String cleanAuthMethod(String method) {
        return method.replaceAll("[{}\"]", "").trim();
    }

    private String cleanGrantType(String grantType) {
        return grantType.replaceAll("[{}\"]", "").trim();
    }

    private String cleanScope(String scope) {
        return scope.replaceAll("[{}\"]", "").trim();
    }

    private String serializeSettings(Object settings) {
        try {
            return objectMapper.writeValueAsString(settings);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации настроек", e);
        }
    }

    private ClientSettings deserializeClientSettings(String json) {
        try {
            Map<String, Object> settings = objectMapper.readValue(json, new TypeReference<>() {});
            return ClientSettings.withSettings(settings).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка десериализации настроек клиента", e);
        }
    }

    private TokenSettings deserializeTokenSettings(String json) {
        try {
            Map<String, Object> rawSettings = objectMapper.readValue(json, new TypeReference<>() {});
            Map<String, Object> processedSettings = processTokenSettings(rawSettings);
            return TokenSettings.withSettings(processedSettings).build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка десериализации настроек токена", e);
        }
    }

    private Map<String, Object> processTokenSettings(Map<String, Object> rawSettings) {
        Map<String, Object> settings = new HashMap<>();

        rawSettings.forEach((key, value) -> {
            if (key.endsWith("time-to-live")) {
                settings.put(key, convertToDuration(value));
            } else if (key.equals("settings.token.access-token-format")) {
                settings.put(key, convertToTokenFormat(value));
            } else if (key.equals("settings.token.id-token-signature-algorithm")) {
                settings.put(key, convertToSignatureAlgorithm(value));
            } else {
                settings.put(key, value);
            }
        });

        return settings;
    }

    private Object convertToDuration(Object value) {
        return value instanceof Number ?
                Duration.ofSeconds(((Number) value).longValue()) : value;
    }

    private Object convertToTokenFormat(Object value) {
        if (value instanceof Map) {
            return new OAuth2TokenFormat(String.valueOf(((Map<?, ?>) value).get("value")));
        }
        return value;
    }

    private Object convertToSignatureAlgorithm(Object value) {
        return value instanceof String ?
                SignatureAlgorithm.from(String.valueOf(value)) : value;
    }
}
