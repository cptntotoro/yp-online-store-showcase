package ru.practicum.mapper.balance;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.dao.balance.UserBalanceDao;
import ru.practicum.dto.UserBalanceResponseDto;
import ru.practicum.model.balance.UserBalance;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserBalanceMapperTest {

    private final UserBalanceMapper userBalanceMapper = Mappers.getMapper(UserBalanceMapper.class);

    @Test
    void shouldMapUserBalanceToUserBalanceDao() {
        UUID userId = UUID.randomUUID();
        UserBalance userBalance = UserBalance.builder()
                .userUuid(userId)
                .amount(BigDecimal.valueOf(100.50))
                .build();

        UserBalanceDao dao = userBalanceMapper.userBalanceToUserBalanceDao(userBalance);

        assertThat(dao).isNotNull();
        assertThat(dao.getUserUuid()).isEqualTo(userId);
        assertThat(dao.getAmount()).isEqualTo(BigDecimal.valueOf(100.50));
    }

    @Test
    void shouldMapUserBalanceDaoToUserBalance() {
        UUID userId = UUID.randomUUID();
        UserBalanceDao dao = UserBalanceDao.builder()
                .userUuid(userId)
                .amount(BigDecimal.valueOf(200.75))
                .build();

        UserBalance userBalance = userBalanceMapper.userBalanceDaoToUserBalance(dao);

        assertThat(userBalance).isNotNull();
        assertThat(userBalance.getUserUuid()).isEqualTo(userId);
        assertThat(userBalance.getAmount()).isEqualTo(BigDecimal.valueOf(200.75));
    }

    @Test
    void shouldMapUserBalanceToUserBalanceResponseDto() {
        UUID userId = UUID.randomUUID();
        UserBalance userBalance = UserBalance.builder()
                .userUuid(userId)
                .amount(BigDecimal.valueOf(300.25))
                .build();

        UserBalanceResponseDto dto = userBalanceMapper.userBalanceToUserBalanceResponseDto(userBalance);

        assertThat(dto).isNotNull();
        assertThat(dto.getBalance()).isEqualTo(BigDecimal.valueOf(300.25));
    }

    @Test
    void shouldHandleNullUserBalance() {
        UserBalanceDao dao = userBalanceMapper.userBalanceToUserBalanceDao(null);
        assertThat(dao).isNull();

        UserBalance userBalance = userBalanceMapper.userBalanceDaoToUserBalance(null);
        assertThat(userBalance).isNull();

        UserBalanceResponseDto dto = userBalanceMapper.userBalanceToUserBalanceResponseDto(null);
        assertThat(dto).isNull();
    }
}