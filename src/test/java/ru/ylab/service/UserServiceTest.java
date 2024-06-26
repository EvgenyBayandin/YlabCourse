package ru.ylab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import ru.ylab.model.User;
import ru.ylab.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Тестовый класс для {@link UserService}.
 * Проверяет функциональность сервиса для работы с пользователями.
 */

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository);
    }

    /**
     * Проверяет создание нового пользователя.
     */

    @Test
    void register_newUser_success() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        User newUser = userService.register("newuser", "password");
        assertNotNull(newUser);
        assertEquals("newuser", newUser.getUsername());
        verify(userRepository).addUser(newUser);
    }

    /**
     * Проверяет создание нового пользователя и выбрасывает исключение если пользователь существует.
     */

    @Test
    void register_existingUser_throwsException() {
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(new User("existinguser", "password", false)));
        assertThrows(IllegalArgumentException.class, () -> userService.register("existinguser", "password"));
    }

    /**
     * Проверяет валидность пароля пользователя.
     */

    @Test
    void login_validCredentials_success() {
        User user = new User("testuser", "password", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        User loggedInUser = userService.login("testuser", "password");
        assertEquals(user, loggedInUser);
    }

    /**
     * Проверяет валидность имени пользователя.
     */

    @Test
    void login_invalidUsername_throwsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.login("nonexistent", "password"));
    }

    /**
     * Проверяет валидность при измении пароля пользователя.
     */

    @Test
    void changePassword_validCredentials_success() {
        User user = new User("testuser", "oldpassword", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        userService.changePassword("testuser", "oldpassword", "newpassword");
        assertEquals("newpassword", user.getPassword());
        verify(userRepository).updateUser(user);
    }

    /**
     * Проверяет валидность при измении пароля пользователя, выбрасывает исключение если пароль существует.
     */

    @Test
    void changePassword_invalidOldPassword_throwsException() {
        User user = new User("testuser", "correctpassword", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class,
                () -> userService.changePassword("testuser", "wrongpassword", "newpassword"));
    }

    /**
     * Проверяет валидность при удалении пользователя
     */

    @Test
    void deleteUser_existingUser_success() {
        User user = new User("testuser", "password", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        userService.deleteUser("testuser");
        verify(userRepository).deleteUser(user);
    }

    /**
     * Проверяет валидность при удалении несуществующего пользователя
     */

    @Test
    void deleteUser_nonexistentUser_throwsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser("nonexistent"));
    }

    /**
     * Проверяет валидность при получении пользователя
     */

    @Test
    void getUser_existingUser_returnsUser() {
        User user = new User("testuser", "password", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        User retrievedUser = userService.getUser("testuser");
        assertEquals(user, retrievedUser);
    }

    /**
     * Проверяет валидность при уполучении пользователя, выбрасывает исключение если пользователь существует.
     */

    @Test
    void getUser_nonexistentUser_throwsException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> userService.getUser("nonexistent"));
    }
}