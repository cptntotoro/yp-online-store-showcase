package ru.practicum.mapper.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.client.dto.UserBalanceResponseDto;
import ru.practicum.dao.user.UserDao;
import ru.practicum.dto.auth.UserAuthDto;
import ru.practicum.model.balance.UserBalance;
import ru.practicum.model.user.User;

/**
 * Маппер пользователей
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Смаппить DAO пользователя в пользователя
     *
     * @param userDao DAO пользователя
     * @return Пользователь
     */
//    @Mapping(target = "accountNonLocked", expression = "java(!userDao.isLocked())")
//    @Mapping(target = "accountNonExpired", expression = "java(!userDao.isExpired())")
//    @Mapping(target = "credentialsNonExpired", expression = "java(!userDao.isCredentialsExpired())")
    User userDaoToUser(UserDao userDao);

    /**
     * Смаппить пользователя в DAO пользователя
     *
     * @param user Пользователь
     * @return DAO пользователя
     */
    UserDao userToUserDao(User user);

    /**
     * Смаппить DTO ответа баланса пользователя в баланс счета пользователя
     *
     * @param userBalanceResponseDto DTO ответа баланса пользователя
     * @return Баланс счета пользователя
     */
    UserBalance userBalanceResponseDtoToUserBalance(UserBalanceResponseDto userBalanceResponseDto);

    /**
     * Смаппить DTO авторизации пользователя в пользователя (для регистрации)
     *
     * @param userAuthDto DTO авторизации пользователя
     * @return Пользователь
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    User userAuthDtoToUser(UserAuthDto userAuthDto);

//    /**
//     * Преобразование списка ролей в строку (для БД)
//     */
//    default String mapRolesToString(List<String> roles) {
//        return roles != null ? String.join(",", roles) : "";
//    }
//
//    /**
//     * Преобразование строки в список ролей (из БД)
//     */
//    default List<String> mapStringToRoles(String roles) {
//        return roles != null && !roles.isEmpty() ? List.of(roles.split(",")) : Collections.emptyList();
//    }
//
//    /**
//     * Смаппить пользователя в UserDetails для Spring Security
//     */
//    default UserDetails userToUserDetails(User user) {
//        List<GrantedAuthority> authorities = user.getRoles().stream()
//                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//                .collect(Collectors.toList());
//
//        return User.builder()
//                        .uuid(user.getUuid())
//                .username(user.getUsername())
//                .email(user.getEmail())
//                .password(user.getPassword())
//                .enabled(user.isEnabled())
//                .roles(mapStringToRoles(mapRolesToString(user.getRoles())))
//                                .build();
//    }
}
