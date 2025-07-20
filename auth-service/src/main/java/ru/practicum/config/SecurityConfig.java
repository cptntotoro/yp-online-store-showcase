package ru.practicum.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import ru.practicum.repository.ReactiveRegisteredClientRepository;
import ru.practicum.repository.ReactiveToBlockingClientRepositoryAdapter;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/oauth2/token").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtDecoder(jwtDecoder())));

        return http.build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return NimbusReactiveJwtDecoder
                .withJwkSetUri("http://localhost:9000/oauth2/jwks")
                .build();
    }

    @Bean
    @Primary
    // Spring Security OAuth2 не поддерживает реактивный репозиторий
    public RegisteredClientRepository registeredClientRepository(ReactiveToBlockingClientRepositoryAdapter adapter) {
        return adapter;
    }

    @Bean
    public ReactiveToBlockingClientRepositoryAdapter reactiveToBlockingClientRepositoryAdapter(
            ReactiveRegisteredClientRepository reactiveRepository) {
        return new ReactiveToBlockingClientRepositoryAdapter(reactiveRepository);
    }
}