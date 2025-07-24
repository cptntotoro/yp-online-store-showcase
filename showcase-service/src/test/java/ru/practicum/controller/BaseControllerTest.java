package ru.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.practicum.config.security.SecurityConfig;
import ru.practicum.dto.cart.CartDto;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.user.User;
import ru.practicum.service.cart.CartService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Базовый тестовый класс контроллеров
 */
@SpringBootTest
@AutoConfigureWebTestClient
@Import(SecurityConfig.class)
public abstract class BaseControllerTest {
    @MockBean
    protected CartService cartService;

    @MockBean
    protected CartMapper cartMapper;

    @Autowired
    protected WebTestClient webTestClient;

    protected final UUID TEST_USER_UUID = UUID.randomUUID();
    protected final User TEST_USER = User.builder()
            .uuid(TEST_USER_UUID)
            .username("testuser")
            .password("password")
            .roles(List.of("USER"))
            .email("test@example.com")
            .enabled(true)
            .accountNonExpired(true)
            .accountNonLocked(true)
            .credentialsNonExpired(true)
            .createdAt(LocalDateTime.now())
            .build();

    @BeforeEach
    protected void baseSetUp() {
        CartDto mockCartDto = new CartDto();
        when(cartService.get(TEST_USER_UUID)).thenReturn(Mono.just(new Cart()));
        when(cartMapper.cartToCartDto(any())).thenReturn(mockCartDto);
    }

    protected WebTestClient getWebTestClientWithMockUser() {
        return webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                new UsernamePasswordAuthenticationToken(
                        TEST_USER,
                        "password",
                        TEST_USER.getAuthorities()
                )
        ));
    }
}
