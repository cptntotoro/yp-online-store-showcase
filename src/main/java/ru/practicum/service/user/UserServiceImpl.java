package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
    public User add() {
        User user = User.builder().username("guest").build();
        user = userRepository.save(user);
        cartService.create(user.getUuid());
        return user;
    }

    @Override
    public boolean existsByUuid(UUID userUuid) {
        return userRepository.existsById(userUuid);
    }
}
