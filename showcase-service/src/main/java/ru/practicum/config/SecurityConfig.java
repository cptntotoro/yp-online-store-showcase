package ru.practicum.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.practicum.service.auth.UserDetailsServiceImpl;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
//@EnableReactiveMethodSecurity // для реактивной метод-безопасности
@RequiredArgsConstructor
public class SecurityConfig {
    private final ReactiveUserDetailsService userDetailsService;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Для REST API, для форм включить
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
//                                "/",
                                "/products/**",
                                "/login",
                                "/sign-up",
                                "/styles/**",
                                "/scripts/**",
                                "/images/**",
                                "/templates/**",
                                "/fragments/**"
                        ).permitAll()
                        .pathMatchers(
                                "/cart/**",
                                "/orders/**",
                                "/checkout",
                                "/payment"
                        ).authenticated()
                        .anyExchange().denyAll() // Блокируем все остальное
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/products"))
//                        .authenticationFailureHandler((exchange, exception) ->
//                                Mono.fromRunnable(() -> {
//                                    ServerWebExchange serverWebExchange = exchange.getExchange();
//                                    String path = serverWebExchange.getRequest().getPath().pathWithinApplication().value();
//                                    serverWebExchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
//                                    serverWebExchange.getResponse().getHeaders().setLocation(
//                                            java.net.URI.create(path + "?error=true")
//                                    );
//                                })
//                        )
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler((exchange, authentication) -> {
                            ServerWebExchange serverWebExchange = exchange.getExchange();
                            // Правильный способ инвалидации сессии в реактивном контексте
                            return serverWebExchange.getSession()
                                    .doOnNext(WebSession::invalidate)
                                    .then(Mono.fromRunnable(() -> {
                                        serverWebExchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.FOUND);
                                        serverWebExchange.getResponse().getHeaders().setLocation(
                                                URI.create("/login?logout=true")
                                        );
                                    }));
                        })
                        .requiresLogout(new ServerWebExchangeMatcher() {
                            @Override
                            public Mono<MatchResult> matches(ServerWebExchange exchange) {
                                exchange.getResponse().addCookie(
                                        ResponseCookie.from("JSESSIONID", "")
                                                .maxAge(0)
                                                .path("/")
                                                .build()
                                );
                                return ServerWebExchangeMatcher.MatchResult.match();
                            }
                        })
                )
                .oauth2Login(oauth2 -> oauth2
                        .authenticationMatcher(new PathPatternParserServerWebExchangeMatcher("/login/oauth2/code/{registrationId}"))
                        .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/"))
                )
                .authenticationManager(authenticationManager())
                .build();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder());
        return manager;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}