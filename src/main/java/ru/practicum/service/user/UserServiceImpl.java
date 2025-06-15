package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.practicum.dao.user.UserDao;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.user.User;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.service.cart.CartService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    /**
     * Репозиторий пользователей
     */
    private final UserRepository userRepository;
    /**
     * Сервис управления корзиной товаров
     */
    private final CartService cartService;

    private final UserMapper userMapper;

    @Override
    @Transactional
    public Mono<User> add() {
        UUID userUuid = UUID.randomUUID();

        User newUser = User.builder()
                .uuid(userUuid)
                .username("guest_" + userUuid.toString().substring(0, 8))
                .createdAt(LocalDateTime.now())
                .build();

        return userRepository.save(userMapper.userToUserDao(newUser))
                .flatMap(savedUser -> {
                    return cartService.createGuest(savedUser.getUuid())
                            .thenReturn(userMapper.userDaoToUser(savedUser));
                })
                .onErrorResume(e -> {
                    return Mono.error(new RuntimeException("Failed to create guest user and cart. Please try again."));
                });
    }

    @Override
    public Mono<Boolean> existsByUuid(UUID userUuid) {
        return userRepository.existsById(userUuid);
    }
}
