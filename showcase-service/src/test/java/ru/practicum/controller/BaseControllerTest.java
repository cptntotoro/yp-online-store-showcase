package ru.practicum.controller;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.thymeleaf.spring6.SpringWebFluxTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.reactive.ThymeleafReactiveViewResolver;
import org.thymeleaf.templatemode.TemplateMode;
import reactor.core.publisher.Mono;
import ru.practicum.dto.cart.CartDto;
import ru.practicum.exception.ControllerExceptionHandler;
import ru.practicum.mapper.cart.CartMapper;
import ru.practicum.model.cart.Cart;
import ru.practicum.service.cart.CartService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Базовый тестовый класс контроллеров
 */
public abstract class BaseControllerTest {
    @Mock
    protected CartService cartService;

    @Mock
    protected CartMapper cartMapper;

    protected WebTestClient webTestClient;

    protected final UUID TEST_USER_UUID = UUID.randomUUID();

    protected abstract Object getController();

    @BeforeEach
    protected void baseSetUp() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.refresh();

        CartDto mockCartDto = new CartDto();

        when(cartService.get(TEST_USER_UUID)).thenReturn(Mono.just(new Cart()));
        when(cartMapper.cartToCartDto(any())).thenReturn(mockCartDto);

        // Настройка веб-клиента
        webTestClient = WebTestClient.bindToController(getController())
                .controllerAdvice(new GlobalControllerAdvice(cartService, cartMapper), new ControllerExceptionHandler())
                .webFilter((exchange, chain) -> {
//                    exchange.getAttributes().put(WebAttributes.USER_UUID, TEST_USER_UUID);
                    return chain.filter(exchange);
                })
                .viewResolvers(registry -> {
                    ThymeleafReactiveViewResolver resolver = new ThymeleafReactiveViewResolver();
                    resolver.setApplicationContext(context);

                    SpringWebFluxTemplateEngine engine = new SpringWebFluxTemplateEngine();
                    SpringResourceTemplateResolver templateResolver = new SpringResourceTemplateResolver();
                    templateResolver.setPrefix("classpath:/web/templates/");
                    templateResolver.setSuffix(".html");
                    templateResolver.setTemplateMode(TemplateMode.HTML);
                    templateResolver.setCharacterEncoding("UTF-8");
                    templateResolver.setCacheable(false);
                    templateResolver.setApplicationContext(context);

                    engine.setTemplateResolver(templateResolver);
                    resolver.setTemplateEngine(engine);
                    resolver.setOrder(1);
                    registry.viewResolver(resolver);
                })
                .configureClient()
                .baseUrl("/")
                .build();
    }
}
