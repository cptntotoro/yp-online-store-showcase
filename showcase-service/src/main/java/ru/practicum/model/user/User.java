package ru.practicum.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Пользователь
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    /**
     * Идентификатор
     */
    private UUID uuid;

    /**
     * Имя пользователя
     */
    private String username;

    /**
     * Пароль
     */
    private String password;

    /**
     * Роль
     */
    private List<String> roles;

    /**
     * Адрес электронной почты
     */
    private String email;

    /**
     * Дата создания
     */
    private LocalDateTime createdAt;

    /**
     * Флаг активности аккаунта
     */
    private boolean enabled;

    /**
     * Флаг, указывающий, не заблокирована ли учетная запись
     */
    private boolean accountNonLocked;

    /**
     * Флаг, указывающий, не истек ли срок действия учетной записи
     */
    private boolean accountNonExpired;

    /**
     * Флаг, указывающий, не истек ли срок действия учетных данных (пароля)
     */
    private boolean credentialsNonExpired;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}