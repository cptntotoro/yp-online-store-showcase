package ru.practicum.mapper.balance;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dao.balance.UserBalanceDao;
import ru.practicum.dto.balance.UserBalanceResponseDto;
import ru.practicum.model.balance.UserBalance;

/**
 * Маппер баланса счета пользователя
 */
@Mapper(componentModel = "spring")
public interface UserBalanceMapper {

    /**
     * Смаппить баланс счета пользователя в DAO баланса пользователя
     *
     * @param userBalance Баланс счета пользователя
     * @return DAO баланса пользователя
     */
    UserBalanceDao userBalanceToUserBalanceDao(UserBalance userBalance);

    /**
     * Смаппить DAO баланса пользователя в баланс счета пользователя
     *
     * @param userBalanceDao DAO баланса пользователя
     * @return Баланс счета пользователя
     */
    UserBalance userBalanceDaoToUserBalance(UserBalanceDao userBalanceDao);

    /**
     * Смаппить баланс счета пользователя в DTO ответа баланса пользователя
     *
     * @param userBalance Баланс счета пользователя
     * @return DTO ответа баланса пользователя
     */
    @Mapping(target = "balance", source = "amount")
    UserBalanceResponseDto userBalanceToUserBalanceResponseDto(UserBalance userBalance);
}