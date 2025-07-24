package ru.practicum.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import reactor.core.publisher.Mono;
import ru.practicum.repository.ReactiveRegisteredClientRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Configuration
public class OAuth2ClientInitializer {

    @Bean
    public ApplicationRunner initializeClients(ReactiveRegisteredClientRepository clientRepository) {
        return args -> {
            clientRepository.findByClientId("showcase-client")
                    .switchIfEmpty(Mono.defer(() -> {
                        RegisteredClient showcaseClient = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("showcase-client")
                                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
                                .clientSecretExpiresAt(Instant.now().plus(Duration.ofHours(1)))
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

            clientRepository.findByClientId("payment-service")
                    .switchIfEmpty(Mono.defer(() -> {
                        RegisteredClient paymentService = RegisteredClient.withId(UUID.randomUUID().toString())
                                .clientId("payment-service")
                                .clientSecret("$2a$10$OuxpJ2wwsMQABCtQX794deWIPqSaqUgevnNiAghcLrTVN44U2xG2a")
                                .clientSecretExpiresAt(Instant.now().plus(Duration.ofHours(1)))
                                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(false).build())
                                .tokenSettings(TokenSettings.builder().build())
                                .build();
                        return clientRepository.save(paymentService).thenReturn(paymentService);
                    }))
                    .subscribe();
        };
    }
}