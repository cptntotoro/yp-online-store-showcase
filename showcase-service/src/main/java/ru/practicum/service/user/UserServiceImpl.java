package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.practicum.exception.auth.UserAlreadyExistsException;
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

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public Mono<User> createGuest() {
        return Mono.defer(() -> {
            User newUser = User.builder()
                    .username("guest")
                    .createdAt(LocalDateTime.now())
                    .build();

            return userRepository.save(userMapper.userToUserDao(newUser))
                    .flatMap(savedUser -> cartService.createGuest(savedUser.getUuid())
                            .thenReturn(savedUser)
                    )
                    .onErrorResume(e -> Mono.error(new RuntimeException("Ошибка создания гостевого пользователя и его корзины", e)))
                    .map(userMapper::userDaoToUser);
        });
    }

    @Override
    @Transactional
    public Mono<User> register(User user) {
        return userRepository.findByUsername(user.getUsername())
                .flatMap(existingUser -> Mono.<User>error(new UserAlreadyExistsException("Пользователь с таким именем уже существует")))
                .switchIfEmpty(Mono.defer(() -> {
                    user.setPassword(passwordEncoder.encode(user.getPassword()));
                    user.setRole("ROLE_USER");
                    user.setCreatedAt(LocalDateTime.now());

                    return userRepository.save(userMapper.userToUserDao(user))
                            .flatMap(savedUser -> cartService.createGuest(savedUser.getUuid())
                                    .thenReturn(savedUser)
                            )
                            .map(userMapper::userDaoToUser);
                }));
//                .onErrorResume(e -> {
//                    if (e instanceof UserAlreadyExistsException) {
//                        return Mono.error(e);
//                    }
//                    return Mono.error(new RuntimeException("Ошибка при регистрации пользователя", e));
//                });
    }

    @Override
    public Mono<Boolean> existsByUuid(UUID userUuid) {
        return userRepository.existsById(userUuid);
    }
}
