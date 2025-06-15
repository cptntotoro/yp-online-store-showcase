package ru.practicum.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.dao.user.UserDao;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.user.User;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.service.cart.CartService;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void add_ShouldCreateNewUserAndCart() {
        UUID userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .uuid(userId)
                .username("guest")
                .createdAt(now)
                .build();

        UserDao savedUser = UserDao.builder()
                .uuid(userId)
                .username("guest")
                .createdAt(now)
                .build();

        when(userMapper.userToUserDao(any())).thenReturn(savedUser);
        when(userRepository.save(any())).thenReturn(Mono.just(savedUser));
        when(cartService.createGuest(userId)).thenReturn(Mono.empty());
        when(userMapper.userDaoToUser(any())).thenReturn(user);

        Mono<User> result = userService.add();

        StepVerifier.create(result)
                .expectNextMatches(userResult ->
                        userResult.getUuid().equals(userId) &&
                                userResult.getUsername().equals("guest")
                )
                .verifyComplete();

        verify(userRepository).save(any());
        verify(cartService).createGuest(userId);
    }

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