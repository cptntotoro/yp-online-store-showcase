package ru.practicum.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.user.User;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.repository.user.UserRoleRepository;

/**
 * Кастомный ReactiveUserDetailsService
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    /**
     * Репозиторий пользователей
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий ролей пользователей
     */
    private final UserRoleRepository userRoleRepository;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Пользователь с username " + username + " не найден")))
                .flatMap(userDao -> userRoleRepository.findRolesByUserUuid(userDao.getUuid())
                        .collectList()
                        .map(roles -> {
                            User user = userMapper.userDaoToUser(userDao);
                            user.setRoles(roles);
                            return user;
                        }));
    }
}