package ru.practicum;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

public class ReactiveToBlockingClientRepositoryAdapter implements RegisteredClientRepository {

    private final ReactiveRegisteredClientRepository reactiveRepository;

    public ReactiveToBlockingClientRepositoryAdapter(ReactiveRegisteredClientRepository reactiveRepository) {
        this.reactiveRepository = reactiveRepository;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        reactiveRepository.save(registeredClient).block();
    }

    @Override
    public RegisteredClient findById(String id) {
        return reactiveRepository.findById(id).block();
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return reactiveRepository.findByClientId(clientId).block();
    }
}