package ru.practicum.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.dao.user.UserDao;
import ru.practicum.repository.user.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements ReactiveUserDetailsService {

    /**
     * Репозиторий пользователей
     */
    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User with username '" + username + "' not found")))
                .map(this::mapToUserDetails);
    }

    private UserDetails mapToUserDetails(UserDao userDao) {
        String role = userDao.getRole() != null ? userDao.getRole().replace("ROLE_", "") : "USER";

        return org.springframework.security.core.userdetails.User.builder()
                .username(userDao.getUsername())
                .password(userDao.getPassword())
                .roles(role)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}