package ru.practicum.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final ReactiveUserDetailsService userDetailsService;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                // Этот компонент сохраняет SecurityContext в реактивной сессии
                //  и автоматически восстанавливает его из сессии при каждом запросе, если она активна.
                //  Такой подход позволяет реализовать stateful-аутентификацию, аналогичную поведению классических веб-приложений.
                // включить сохранение и восстановление SecurityContext через сессию
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Для REST API, для форм включить
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(
                                "/favicon.ico",
                                "/login",
                                "/logout",
                                "/notfound",
                                "/error",
                                "/sign-up",
                                "/styles/**",
                                "/scripts/**",
                                "/images/**",
                                "/templates/**",
                                "/fragments/**"
                        ).permitAll()
                                .pathMatchers(HttpMethod.GET, "/products", "/products/**").permitAll()
                                .pathMatchers(
                                        "/cart/**",
                                        "/orders/**",
                                        "/checkout",
                                        "/payment"
                                ).authenticated()
                                .anyExchange().authenticated()
                )
                .formLogin(form -> form
                                .loginPage("/login") // неудачная — на /login?error.
                                .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/products"))
                                .authenticationFailureHandler((webFilterExchange, exception) -> {
                                    return Mono.fromRunnable(() -> {
                                        ServerWebExchange serverWebExchange = webFilterExchange.getExchange();
                                        serverWebExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                                        serverWebExchange.getResponse().getHeaders().setLocation(
                                                // TODO
                                                URI.create("/login?error")
                                        );
                                }))

//                        Таким образом, после логина в сессии пользователя будут сохранены:
                        //        JSESSIONID (cookie, идентификатор сессии);
                        //        SecurityContext (в реактивной обёртке — через ReactiveSecurityContextHolder);
                        //        Authentication (внутри SecurityContext).


                        // Однако в приложениях с кастомной аутентификацией (например, при использовании собственного AuthenticationWebFilter, ServerAuthenticationSuccessHandler или когда SecurityContext сохраняется вручную) Spring уже не гарантирует смену ID сессии. В этом случае защита от фиксации ложится на вас.
                        //Чтобы явно инициировать создание новой сессии, достаточно вызвать:
                        //
                        //exchange.getSession()
                        //    .flatMap(WebSession::invalidate) 

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
                                // Регистрируем OidcClientInitiatedServerLogoutSuccessHandler
//                                .logoutSuccessHandler(oidcLogoutSuccessHandler())
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


                        // вы можете дополнительно:
                        //удалить куки;
                        //записать информацию о выходе в лог;
                        //отправить событие;
                        //вернуть JSON-ответ вместо пустого тела.

//                        .requiresLogout(new ServerWebExchangeMatcher() {
//                            @Override
//                            public Mono<MatchResult> matches(ServerWebExchange exchange) {
//                                exchange.getResponse().addCookie(
//                                        ResponseCookie.from("JSESSIONID", "")
//                                                .maxAge(0)
//                                                .path("/")
//                                                .build()
//                                );
//                                return ServerWebExchangeMatcher.MatchResult.match();
//                            }
//                        })
                )
//                .oauth2Login(oauth2 -> oauth2
//                        .authenticationMatcher(new PathPatternParserServerWebExchangeMatcher("/login/oauth2/code/{registrationId}"))
//                        .authenticationSuccessHandler(new RedirectServerAuthenticationSuccessHandler("/"))
//                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authenticationManager(authenticationManager())
//                .oauth2Login(Customizer.withDefaults())
//                .oauth2Client(Customizer.withDefaults())
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((exchange, ex) -> {
                            exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                            exchange.getResponse().getHeaders().setLocation(URI.create("/notfound"));
                            return Mono.empty();
                        })
                )
                .build();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        manager.setPasswordEncoder(passwordEncoder());
        return manager;
    }

//    @Bean
//    public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
//        return username -> userRepository.findByUsername(username)
//                .map(user -> User.withUsername(user.getUsername())
//                        .password(user.getPassword())
//                        .roles("USER")
//                        .build());
//    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    // Источник описаний клиентов с одним статическим клиентом Google.
//    @Bean
//    public ReactiveClientRegistrationRepository clientRegistrationRepository() {
//        return new InMemoryReactiveClientRegistrationRepository(this.googleClientRegistration());
//    }

    // Сервис для выполнения авторизации клиентов, а также их хранения на время жизни токена.
//    @Bean
//    public ReactiveOAuth2AuthorizedClientService authorizedClientService(
//            ReactiveClientRegistrationRepository clientRegistrationRepository) {
//        return new InMemoryReactiveOAuth2AuthorizedClientService(clientRegistrationRepository);
//    }

    // Репозиторий-обёртка для ReactiveOAuth2AuthorizedClientService
//    @Bean
//    public ServerOAuth2AuthorizedClientRepository authorizedClientRepository(
//            ReactiveOAuth2AuthorizedClientService authorizedClientService) {
//        return new AuthenticatedPrincipalServerOAuth2AuthorizedClientRepository(authorizedClientService);
//    }

//    private ClientRegistration googleClientRegistration() {
//        return CommonOAuth2Provider.GOOGLE.getBuilder("google")
//                .clientId("google-client-id")
//                .clientSecret("google-client-secret")
//                .build();
//    }

//    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler() {
//        // Для работы необходимо указать ReactiveClientRegistrationRepository
//        // Этот репозиторий содержит информацию обо всех зарегистрированных в приложении клиентах (и их провайдерах).
//        OidcClientInitiatedServerLogoutSuccessHandler handler =
//                new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
//
//        // Можно указать адрес после успешного логаута
//        handler.setPostLogoutRedirectUri("{baseUrl}");
//
//        return handler;
//    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}