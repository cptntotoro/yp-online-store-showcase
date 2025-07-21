package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.practicum.dao.user.UserDao;
import ru.practicum.exception.auth.UserAlreadyExistsException;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.user.User;
import ru.practicum.repository.user.RoleRepository;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.repository.user.UserRoleRepository;
import ru.practicum.service.cart.CartService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /**
     * Репозиторий пользователей
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий ролей пользователей
     */
    private final UserRoleRepository userRoleRepository;

    /**
     * Репозиторий пользовательских ролей
     */
    private final RoleRepository roleRepository;

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
    public Mono<User> register(User user) {
        return userRepository.existsByUsername(user.getUsername())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new UserAlreadyExistsException("Пользователь с таким username уже существует"));
                    }
                    return userRepository.existsByEmail(user.getEmail());
                })
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new UserAlreadyExistsException("Пользователь с таким email уже существует"));
                    }

                    UserDao userDao = UserDao.builder()
                            .username(user.getUsername())
                            .password(passwordEncoder.encode(user.getPassword()))
                            .email(user.getEmail())
                            .enabled(true)
                            .accountNonLocked(true)
                            .accountNonExpired(true)
                            .credentialsNonExpired(true)
                            .build();

                    return userRepository.save(userDao)
                            .flatMap(savedUser -> roleRepository.findByName("USER")
                                    .flatMap(role -> userRoleRepository.addRoleToUser(savedUser.getUuid(), role.getUuid())
                                            .then(cartService.createGuest(savedUser.getUuid()))
                                            .thenReturn(savedUser))
                                    .map(userMapper::userDaoToUser));
                });
    }

    @Override
    public Mono<Boolean> existsByUuid(UUID userUuid) {
        return userRepository.existsById(userUuid);
    }

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
