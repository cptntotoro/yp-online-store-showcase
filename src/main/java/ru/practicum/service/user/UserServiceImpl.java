package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.practicum.model.user.User;
import ru.practicum.repository.user.UserRepository;
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
     * Сервис управления корзиной товаров
     */
    private final CartService cartService;

    @Override
    @Transactional
    public Mono<User> add() {
        return Mono.just(User.builder().username("guest").build())
                .flatMap(userRepository::save)
                .flatMap(user -> cartService.createGuest(user.getUuid())
                        .then(userRepository.findById(user.getUuid()))
                        .onErrorResume(e -> Mono.error(new RuntimeException("Ошибка создания гостевого пользователя и его корзины.")))
                );
    }

    @Override
    public Mono<Boolean> existsByUuid(UUID userUuid) {
        return userRepository.existsById(userUuid);
    }
}
