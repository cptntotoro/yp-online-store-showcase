package ru.practicum;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Configuration
public class ClientInitializer {

    @Bean
    public ApplicationRunner initializeClients(ReactiveRegisteredClientRepository clientRepository) {
        return args -> {
            // Register showcase-service client
            clientRepository.findByClientId("showcase-client")
                    .switchIfEmpty(Mono.defer(() -> {
                        RegisteredClient showcaseClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("showcase-client")
                                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .scope("payment.read")
                                .scope("payment.write")
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .tokenSettings(TokenSettings.builder().build())
                                .build();
                        return clientRepository.save(showcaseClient).thenReturn(showcaseClient);
                    }))
                    .subscribe();

            // Register payment-service resource server
            clientRepository.findByClientId("payment-service")
                    .switchIfEmpty(Mono.defer(() -> {
                        RegisteredClient paymentService = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("payment-service")
                                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .scope("payment.read")
                                .scope("payment.write")
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .tokenSettings(TokenSettings.builder().build())
                                .build();
                        return clientRepository.save(paymentService).thenReturn(paymentService);
                    }))
                    .subscribe();
        };
    }
}