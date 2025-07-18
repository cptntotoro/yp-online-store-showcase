package ru.practicum.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AnonymousAuthenticationWebFilter;
import org.springframework.security.web.server.authentication.DelegatingServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationFailureHandler;
import org.springframework.security.web.server.authentication.RedirectServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.practicum.config.rememberme.RememberMeAuthenticationConverter;
import ru.practicum.config.rememberme.RememberMeAuthenticationWebFilter;
import ru.practicum.config.rememberme.RememberMeSuccessHandler;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    /**
     * Кастомный ReactiveUserDetailsService
     */
    private final ReactiveUserDetailsService userDetailsService;

    /**
     * Remember me ключ
     */
    private final String rememberMeKey = "super-secret-key";

    /**
     * Время действия токена (2 недели в секундах)
     */
    int tokenValidity = 1209600;

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
//                        .pathMatchers("/public/**").permitAll()
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
                        .loginPage("/login")
                        .authenticationSuccessHandler(new DelegatingServerAuthenticationSuccessHandler(
                                rememberMeSuccessHandler(),
                                new RedirectServerAuthenticationSuccessHandler("/products")
                        ))
                        .authenticationFailureHandler(new RedirectServerAuthenticationFailureHandler("/login?error"))
                )
                .addFilterAt(new RememberMeAuthenticationWebFilter(
                        rememberMeConverter(),
                        new AnonymousAuthenticationWebFilter("anonymous")
                ), SecurityWebFiltersOrder.AUTHENTICATION)
                .logout(logout -> logout
                                .logoutUrl("/logout")
                                .requiresLogout(new PathPatternParserServerWebExchangeMatcher("/logout"))
                                .logoutSuccessHandler((exchange, auth) -> {
                                    ServerWebExchange swe = exchange.getExchange();
                                    // Очищаем remember-me куку
                                    ResponseCookie cookie = ResponseCookie.from("remember-me", "")
                                            .maxAge(0)
                                            .path("/")
                                            .httpOnly(true)
                                            .build();
                                    swe.getResponse().addCookie(cookie);

                                    return swe.getSession()
                                            .doOnNext(WebSession::invalidate)
                                            .then(Mono.fromRunnable(() -> {
                                                swe.getResponse().setStatusCode(HttpStatus.FOUND);
                                                swe.getResponse().getHeaders().setLocation(URI.create("/login?logout"));
                                            }));
                                })
                )
//                .oauth2Login(withDefaults())
//                .oauth2Client(withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authenticationManager(authenticationManager())
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

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return NoOpPasswordEncoder.getInstance(); // Только для тестирования!
//    }

    @Bean
    public RememberMeAuthenticationConverter rememberMeConverter() {
        return new RememberMeAuthenticationConverter(
                userDetailsService,
                passwordEncoder(),
                rememberMeKey
        );
    }

    @Bean
    public RememberMeSuccessHandler rememberMeSuccessHandler() {
        return new RememberMeSuccessHandler(rememberMeKey, tokenValidity);
    }
}