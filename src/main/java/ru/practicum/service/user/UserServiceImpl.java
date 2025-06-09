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
        User newUser = User.builder()
                .uuid(UUID.randomUUID())
                .username("guest")
                .createdAt(LocalDateTime.now())
                .build();

        UserDao newUserDao = userMapper.userToUserDao(newUser);

        return userRepository.save(newUserDao)
                .flatMap(savedDao -> cartService.createGuest(savedDao.getUuid())
                        .thenReturn(userMapper.userDaoToUser(savedDao)))
                .onErrorResume(e -> Mono.error(new RuntimeException("Ошибка создания гостевого пользователя и его корзины.", e)));
    }

    @Override
    public Mono<Boolean> existsByUuid(UUID userUuid) {
        return userRepository.existsById(userUuid);
    }
}
