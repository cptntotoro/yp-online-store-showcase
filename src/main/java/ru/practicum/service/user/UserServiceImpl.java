package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.model.user.User;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.service.cart.CartService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CartService cartService;

    @Override
    public User add() {
        User user = User.builder().username("guest").build();
        user = userRepository.save(user);
        cartService.create(user.getUuid());
        return user;
    }
}
