package ru.practicum;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import reactor.core.publisher.Mono;

public interface ReactiveRegisteredClientRepository {
    Mono<Void> save(RegisteredClient registeredClient);
    Mono<RegisteredClient> findById(String id);
    Mono<RegisteredClient> findByClientId(String clientId);
}