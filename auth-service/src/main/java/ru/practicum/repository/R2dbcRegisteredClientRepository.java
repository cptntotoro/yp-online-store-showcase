package ru.practicum.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.practicum.dao.RegisteredClientDao;
import ru.practicum.mapper.RegisteredClientMapper;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@RequiredArgsConstructor
public class R2dbcRegisteredClientRepository implements ReactiveRegisteredClientRepository {

    /**
     * Маппер регистрации OAuth2 клиентов
     */
    private final RegisteredClientMapper registeredClientMapper;

    private final R2dbcEntityTemplate entityTemplate;

    @Override
    public Mono<Void> save(RegisteredClient registeredClient) {
        RegisteredClientDao entity = registeredClientMapper.fromRegisteredClient(registeredClient);
        return entityTemplate.insert(entity).then();
    }

    @Override
    public Mono<RegisteredClient> findById(String id) {
        return entityTemplate.selectOne(
                        query(where("id").is(id)),
                        RegisteredClientDao.class)
                .map(registeredClientMapper::toRegisteredClient);
    }

    @Override
    public Mono<RegisteredClient> findByClientId(String clientId) {
        return entityTemplate.selectOne(
                        query(where("client_id").is(clientId)),
                        RegisteredClientDao.class)
                .map(registeredClientMapper::toRegisteredClient);
    }
}