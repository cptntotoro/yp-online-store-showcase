package ru.practicum.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
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
import ru.practicum.config.security.rememberme.RememberMeAuthenticationConverter;
import ru.practicum.config.security.rememberme.RememberMeAuthenticationWebFilter;
import ru.practicum.config.security.rememberme.RememberMeCookieUtil;
import ru.practicum.config.security.rememberme.RememberMeProperties;
import ru.practicum.config.security.rememberme.RememberMeSuccessHandler;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
@EnableConfigurationProperties(RememberMeProperties.class)
public class SecurityConfig {
    /**
     * Кастомный ReactiveUserDetailsService
     */
    private final ReactiveUserDetailsService userDetailsService;

    /**
     * Создает токен при успешном входе
     */
    private final RememberMeSuccessHandler rememberMeSuccessHandler;

    /**
     * Извлекает токен из куки
     */
    private final RememberMeAuthenticationConverter rememberMeAuthenticationConverter;

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .securityContextRepository(new WebSessionServerSecurityContextRepository())
                .csrf(ServerHttpSecurity.CsrfSpec::disable) // Для REST API, для форм включить
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(PermittedPaths.PATTERNS.toArray(String[]::new)).permitAll()
                                .pathMatchers(HttpMethod.GET, "/products", "/products/**")
                                .access((mono, context) -> {
                                    // Разрешить доступ всем, но запустить фильтры (включая remember-me)
                                    return mono.map(auth -> new AuthorizationDecision(true))
                                            .defaultIfEmpty(new AuthorizationDecision(true));
                                })
                        .anyExchange().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .authenticationSuccessHandler(new DelegatingServerAuthenticationSuccessHandler(
                                rememberMeSuccessHandler,
                                new RedirectServerAuthenticationSuccessHandler("/products")
                        ))
                        .authenticationFailureHandler(new RedirectServerAuthenticationFailureHandler("/login?error"))
                )
                .addFilterAt(new RememberMeAuthenticationWebFilter(
                        rememberMeAuthenticationConverter,
                        new AnonymousAuthenticationWebFilter("anonymous")
                ), SecurityWebFiltersOrder.SECURITY_CONTEXT_SERVER_WEB_EXCHANGE)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .requiresLogout(new PathPatternParserServerWebExchangeMatcher("/logout"))
                        .logoutSuccessHandler((exchange, auth) -> {
                            ServerWebExchange swe = exchange.getExchange();
                            ResponseCookie cookie = RememberMeCookieUtil.clearRememberMeCookie();
                            swe.getResponse().addCookie(cookie);

                            return swe.getSession()
                                    .doOnNext(WebSession::invalidate)
                                    .then(Mono.fromRunnable(() -> {
                                        swe.getResponse().setStatusCode(HttpStatus.FOUND);
                                        swe.getResponse().getHeaders().setLocation(URI.create("/login?logout"));
                                    }));
                        })
                )
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
}