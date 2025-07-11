package ru.practicum.mapper.user;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.practicum.client.dto.UserBalanceResponseDto;
import ru.practicum.dao.user.UserDao;
import ru.practicum.model.balance.UserBalance;
import ru.practicum.model.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private final UUID testUuid = UUID.randomUUID();
    private final String testUsername = "testUser";
    private final String testEmail = "test@example.com";
    private final LocalDateTime testCreatedAt = LocalDateTime.now();

    @Test
    void userDaoToUser_ShouldMapAllFieldsCorrectly() {
        UserDao userDao = UserDao.builder()
                .uuid(testUuid)
                .username(testUsername)
                .email(testEmail)
                .createdAt(testCreatedAt)
                .build();

        User user = userMapper.userDaoToUser(userDao);

        assertNotNull(user);
        assertEquals(testUuid, user.getUuid());
        assertEquals(testUsername, user.getUsername());
        assertEquals(testEmail, user.getEmail());
        assertEquals(testCreatedAt, user.getCreatedAt());
    }

    @Test
    void userDaoToUser_ShouldReturnNull_WhenInputIsNull() {
        assertNull(userMapper.userDaoToUser(null));
    }

    @Test
    void userToUserDao_ShouldMapAllFieldsCorrectly() {
        User user = User.builder()
                .uuid(testUuid)
                .username(testUsername)
                .email(testEmail)
                .createdAt(testCreatedAt)
                .build();

        UserDao userDao = userMapper.userToUserDao(user);

        assertNotNull(userDao);
        assertEquals(testUuid, userDao.getUuid());
        assertEquals(testUsername, userDao.getUsername());
        assertEquals(testEmail, userDao.getEmail());
        assertEquals(testCreatedAt, userDao.getCreatedAt());
    }

    @Test
    void userToUserDao_ShouldReturnNull_WhenInputIsNull() {
        assertNull(userMapper.userToUserDao(null));
    }

    @Test
    void userDaoToUser_ShouldHandlePartialData() {
        UserDao userDao = UserDao.builder()
                .uuid(testUuid)
                .username(testUsername)
                .build();

        User user = userMapper.userDaoToUser(userDao);

        assertNotNull(user);
        assertEquals(testUuid, user.getUuid());
        assertEquals(testUsername, user.getUsername());
        assertNull(user.getEmail());
        assertNull(user.getCreatedAt());
    }

    @Test
    void userToUserDao_ShouldHandlePartialData() {
        User user = User.builder()
                .uuid(testUuid)
                .email(testEmail)
                .build();

        UserDao userDao = userMapper.userToUserDao(user);

        assertNotNull(userDao);
        assertEquals(testUuid, userDao.getUuid());
        assertEquals(testEmail, userDao.getEmail());
        assertNull(userDao.getUsername());
        assertNull(userDao.getCreatedAt());
    }

    @Test
    void userBalanceResponseDtoToUserBalance_ShouldMapAllFieldsCorrectly() {
        UUID userId = UUID.randomUUID();
        BigDecimal balance = new BigDecimal("1000.50");
        UserBalanceResponseDto dto = new UserBalanceResponseDto().userUuid(userId).balance(balance);

        UserBalance result = userMapper.userBalanceResponseDtoToUserBalance(dto);

        assertNotNull(result);
        assertEquals(userId, result.getUserUuid());
        assertEquals(balance, result.getBalance());
    }

    @Test
    void userBalanceResponseDtoToUserBalance_ShouldReturnNull_WhenInputIsNull() {
        assertNull(userMapper.userBalanceResponseDtoToUserBalance(null));
    }

    @Test
    void userBalanceResponseDtoToUserBalance_ShouldHandlePartialData() {
        UUID userId = UUID.randomUUID();
        UserBalanceResponseDto dto = new UserBalanceResponseDto().userUuid(userId).balance(null);

        UserBalance result = userMapper.userBalanceResponseDtoToUserBalance(dto);

        assertNotNull(result);
        assertEquals(userId, result.getUserUuid());
        assertNull(result.getBalance());
    }
}