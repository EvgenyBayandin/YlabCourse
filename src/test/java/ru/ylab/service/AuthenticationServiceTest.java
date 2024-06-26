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
 * Тестовый класс для {@link AuthenticationService}.
 * Проверяет функциональность сервиса аутентификации.
 */

class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authenticationService = new AuthenticationService(userRepository);
    }

    /**
     * Проверяет валидность аутентификации существующего пользователя.
     */

    @Test
    void authenticate_validCredentials_shouldReturnUser() {
        User user = new User("testuser", "password", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User authenticatedUser = authenticationService.authenticate("testuser", "password");
        assertEquals(user, authenticatedUser);
        assertTrue(authenticationService.isAuthenticated());
    }

    /**
     * Проверяет аутентификация не существующего пользователя.
     */

    @Test
    void authenticate_invalidUsername_shouldThrowException() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate("nonexistent", "password"));
    }

    /**
     * Проверяет аутентификация не верного парооля пользователя.
     */

    @Test
    void authenticate_invalidPassword_shouldThrowException() {
        User user = new User("testuser", "correctpassword", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate("testuser", "wrongpassword"));
    }

    /**
     * Проверяет аутентифицирован пользователь или нет, возвращает true.
     */

    @Test
    void isAuthenticated_afterSuccessfulAuthentication_shouldReturnTrue() {
        User user = new User("testuser", "password", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        authenticationService.authenticate("testuser", "password");
        assertTrue(authenticationService.isAuthenticated());
    }

    /**
     * Проверяет аутентифицирован пользователь или нет, возвращает false.
     */

    @Test
    void isAuthenticated_beforeAuthentication_shouldReturnFalse() {
        assertFalse(authenticationService.isAuthenticated());
    }

    /**
     * Проверяет получение аутентифицированного пользователя.
     */

    @Test
    void getCurrentUser_whenAuthenticated_shouldReturnUser() {
        User user = new User("testuser", "password", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        authenticationService.authenticate("testuser", "password");
        User currentUser = authenticationService.getCurrentUser();
        assertEquals(user, currentUser);
    }

    /**
     * Проверяет получение не аутентифицированного пользователя.
     */

    @Test
    void getCurrentUser_whenNotAuthenticated_shouldThrowException() {
        assertThrows(IllegalStateException.class, () -> authenticationService.getCurrentUser());
    }

    /**
     * Проверяет разлогинивание пользователя.
     */

    @Test
    void logout_shouldClearCurrentUser() {
        User user = new User("testuser", "password", false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        authenticationService.authenticate("testuser", "password");
        assertTrue(authenticationService.isAuthenticated());

        authenticationService.logout();
        assertFalse(authenticationService.isAuthenticated());
    }
}