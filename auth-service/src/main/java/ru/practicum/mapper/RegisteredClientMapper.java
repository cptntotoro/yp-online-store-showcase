package ru.practicum.mapper;

import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import ru.practicum.dao.RegisteredClientDao;

/**
 * Маппер регистрации OAuth2 клиентов
 */
public interface RegisteredClientMapper {

    /**
     * Смаппить регистрацию OAuth2 клиента в DAO
     *
     * @param registeredClient Регистрация OAuth2 клиента
     * @return DAO регистрации OAuth2 клиента
     */
    RegisteredClientDao fromRegisteredClient(RegisteredClient registeredClient);

    /**
     * Смаппить DAO регистрации OAuth2 клиента в регистрацию OAuth2 клиента
     *
     * @param registeredClientDao DAO регистрации OAuth2 клиента
     * @return Регистрация OAuth2 клиента
     */
    RegisteredClient toRegisteredClient(RegisteredClientDao registeredClientDao);
}
