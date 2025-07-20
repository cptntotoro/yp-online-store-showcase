package ru.practicum.dao;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.time.Instant;
import java.util.Set;

/**
 * DAO регистрации OAuth2 клиента
 */
@Data
@Table("oauth2_registered_clients")
@NoArgsConstructor
public class RegisteredClientDao {

    /**
     * Идентификатор регистрации
     * {@link RegisteredClient#getId()}
     */
    @Id
    private String id;

    /**
     * Идентификатор клиента
     * {@link RegisteredClient#getClientId()}
     */
    private String clientId;

    /**
     * Время создания идентификатора клиента
     * {@link RegisteredClient#getClientIdIssuedAt()}
     */
    private Instant clientIdIssuedAt;

    /**
     * Секрет клиента
     * {@link RegisteredClient#getClientSecret()}
     */
    private String clientSecret;

    /**
     * Время действия секрета клиента
     * {@link RegisteredClient#getClientSecretExpiresAt()}
     */
    private Instant clientSecretExpiresAt;

    /**
     * Имя клиента
     * {@link RegisteredClient#getClientName()}
     */
    private String clientName;

    /**
     * Методы аутентификации клиента
     * {@link RegisteredClient#getClientAuthenticationMethods()}
     */
    private Set<String> clientAuthenticationMethods;

    /**
     * Типы авторизации клиента
     * {@link RegisteredClient#getAuthorizationGrantTypes()}
     */
    private Set<String> authorizationGrantTypes;

    /**
     * URI редиректа
     * {@link RegisteredClient#getRedirectUris()}
     */
    @Transient
    private Set<String> redirectUris;

    /**
     * Json строка для URI редиректа
     * {@link RegisteredClient#getRedirectUris()}
     */
    private String redirectUrisJson;

    /**
     * Скоупы
     * {@link RegisteredClient#getScopes()}
     */
    private Set<String> scopes;

    /**
     * Настройки клиента
     * {@link RegisteredClient#getClientSettings()}
     */
    private String clientSettings;

    /**
     * Настройки токена
     * {@link RegisteredClient#getTokenSettings()}
     */
    private String tokenSettings;
}