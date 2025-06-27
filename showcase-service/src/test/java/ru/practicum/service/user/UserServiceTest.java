package ru.practicum.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.service.cart.CartService;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;


    @Test
    void existsByUuid_ShouldReturnTrue_WhenUserExists() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(Mono.just(true));

        Mono<Boolean> result = userService.existsByUuid(userId);

        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void existsByUuid_ShouldReturnFalse_WhenUserNotExists() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(Mono.just(false));

        Mono<Boolean> result = userService.existsByUuid(userId);

        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();
    }
}