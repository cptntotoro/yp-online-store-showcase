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
    @Mapping(target = "roles", ignore = true)
    User userDaoToUser(UserDao userDao);

    /**
     * Смаппить пользователя в DAO пользователя
     *
     * @param user Пользователь
     * @return DAO пользователя
     */
    @Mapping(target = "uuid", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
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
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    User userAuthDtoToUser(UserAuthDto userAuthDto);
}
