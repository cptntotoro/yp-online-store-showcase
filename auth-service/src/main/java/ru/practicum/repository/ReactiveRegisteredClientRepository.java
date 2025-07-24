package ru.practicum.repository;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import reactor.core.publisher.Mono;

/**
 * Репозиторий регистраций клиентов
 */
public interface ReactiveRegisteredClientRepository {
    /**
     * Сохранить регистрацию клиента
     *
     * @param registeredClient Регистрация клиента {@link RegisteredClient}
     */
    Mono<Void> save(RegisteredClient registeredClient);

    /**
     * Получить регистрацию клиента по идентификатору регистрации
     *
     * @param id Идентификатор регистрации {@link RegisteredClient#getId()}
     * @return Регистрация клиента {@link RegisteredClient}
     */
    Mono<RegisteredClient> findById(String id);

    /**
     * Получить регистрацию клиента по идентификатору клиента
     *
     * @param clientId Идентификатор клиента {@link RegisteredClient#getClientId()}
     * @return Регистрация клиента {@link RegisteredClient}
     */
    Mono<RegisteredClient> findByClientId(String clientId);
}