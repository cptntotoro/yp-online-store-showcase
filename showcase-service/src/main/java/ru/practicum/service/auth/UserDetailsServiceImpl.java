package ru.practicum.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    /**
     * Репозиторий пользователей
     */
    private final UserRepository userRepository;

    /**
     * Маппер пользователей
     */
    private final UserMapper userMapper;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User with username '" + username + "' not found")))
                .map(userMapper::userDaoToUser)
                .map(userMapper::userToUserDetails);
    }
}