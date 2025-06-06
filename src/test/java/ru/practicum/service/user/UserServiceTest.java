package ru.practicum.service.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.model.cart.Cart;
import ru.practicum.model.user.User;
import ru.practicum.repository.user.UserRepository;
import ru.practicum.service.cart.CartService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void add_ShouldCreateNewUserAndCart() {
        User expectedUser = User.builder()
                .uuid(UUID.randomUUID())
                .username("guest")
                .build();

        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        when(cartService.create(any(UUID.class))).thenReturn(new Cart());

        User actualUser = userService.add();

        assertNotNull(actualUser);
        assertEquals("guest", actualUser.getUsername());
        assertNotNull(actualUser.getUuid());

        verify(userRepository, times(1)).save(any(User.class));
        verify(cartService, times(1)).create(actualUser.getUuid());
    }

    @Test
    void existsByUuid_WhenUserExists_ShouldReturnTrue() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);

        boolean exists = userService.existsByUuid(userId);

        assertTrue(exists);
        verify(userRepository, times(1)).existsById(userId);
    }

    @Test
    void existsByUuid_WhenUserNotExists_ShouldReturnFalse() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        boolean exists = userService.existsByUuid(userId);

        assertFalse(exists);
        verify(userRepository, times(1)).existsById(userId);
    }
}