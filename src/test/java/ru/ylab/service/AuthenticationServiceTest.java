package ru.ylab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ylab.model.User;
import ru.ylab.repository.UserRepository;
import ru.ylab.util.DatabaseManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AuthenticationService.
 * These tests use Testcontainers to spin up a PostgreSQL database for each test.
 * Ensure Docker is running on your machine before executing these tests.
 */
@Testcontainers
class AuthenticationServiceTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private AuthenticationService authenticationService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws SQLException {
        Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        DatabaseManager testDatabaseManager = new TestDatabaseManager(connection);
        userRepository = new UserRepository(testDatabaseManager);
        authenticationService = new AuthenticationService(userRepository);

        // Очищаем таблицу users
        try (var statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE users");
        }

        // Добавляем тестового пользователя
        User testUser = new User("testuser", "password", false);
        userRepository.addUser(testUser);
    }

    @Test
    void testAuthenticate() throws SQLException {
        User authenticatedUser = authenticationService.authenticate("testuser", "password");
        assertNotNull(authenticatedUser);
        assertEquals("testuser", authenticatedUser.getUsername());
    }

    @Test
    void testAuthenticateWrongPassword() {
        assertThrows(IllegalArgumentException.class, () ->
                authenticationService.authenticate("testuser", "wrongpassword")
        );
    }

    @Test
    void testAuthenticateNonExistentUser() {
        assertThrows(IllegalArgumentException.class, () ->
                authenticationService.authenticate("nonexistent", "password")
        );
    }

    @Test
    void testIsAuthenticated() throws SQLException {
        assertFalse(authenticationService.isAuthenticated());
        authenticationService.authenticate("testuser", "password");
        assertTrue(authenticationService.isAuthenticated());
    }

    @Test
    void testGetCurrentUser() throws SQLException {
        authenticationService.authenticate("testuser", "password");
        User currentUser = authenticationService.getCurrentUser();
        assertNotNull(currentUser);
        assertEquals("testuser", currentUser.getUsername());
    }

    @Test
    void testGetCurrentUserNotAuthenticated() {
        assertThrows(IllegalStateException.class, () ->
                authenticationService.getCurrentUser()
        );
    }

    @Test
    void testLogout() throws SQLException {
        authenticationService.authenticate("testuser", "password");
        assertTrue(authenticationService.isAuthenticated());
        authenticationService.logout();
        assertFalse(authenticationService.isAuthenticated());
    }

    static class TestDatabaseManager extends DatabaseManager {
        private static Connection connection = null;

        public TestDatabaseManager(Connection connection) {
            TestDatabaseManager.connection = connection;
        }

        public static Connection getConnection() throws SQLException {
            return connection;
        }
    }
}

//package ru.ylab.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//
//import java.util.Optional;
//import ru.ylab.model.User;
//import ru.ylab.repository.UserRepository;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
///**
// * Тестовый класс для {@link AuthenticationService}.
// * Проверяет функциональность сервиса аутентификации.
// */
//
//class AuthenticationServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    private AuthenticationService authenticationService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        authenticationService = new AuthenticationService(userRepository);
//    }
//
//    /**
//     * Проверяет валидность аутентификации существующего пользователя.
//     */
//
//    @Test
//    void authenticate_validCredentials_shouldReturnUser() {
//        User user = new User("testuser", "password", false);
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
//
//        User authenticatedUser = authenticationService.authenticate("testuser", "password");
//        assertEquals(user, authenticatedUser);
//        assertTrue(authenticationService.isAuthenticated());
//    }
//
//    /**
//     * Проверяет аутентификация не существующего пользователя.
//     */
//
//    @Test
//    void authenticate_invalidUsername_shouldThrowException() {
//        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
//
//        assertThrows(IllegalArgumentException.class,
//                () -> authenticationService.authenticate("nonexistent", "password"));
//    }
//
//    /**
//     * Проверяет аутентификация не верного парооля пользователя.
//     */
//
//    @Test
//    void authenticate_invalidPassword_shouldThrowException() {
//        User user = new User("testuser", "correctpassword", false);
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
//
//        assertThrows(IllegalArgumentException.class,
//                () -> authenticationService.authenticate("testuser", "wrongpassword"));
//    }
//
//    /**
//     * Проверяет аутентифицирован пользователь или нет, возвращает true.
//     */
//
//    @Test
//    void isAuthenticated_afterSuccessfulAuthentication_shouldReturnTrue() {
//        User user = new User("testuser", "password", false);
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
//
//        authenticationService.authenticate("testuser", "password");
//        assertTrue(authenticationService.isAuthenticated());
//    }
//
//    /**
//     * Проверяет аутентифицирован пользователь или нет, возвращает false.
//     */
//
//    @Test
//    void isAuthenticated_beforeAuthentication_shouldReturnFalse() {
//        assertFalse(authenticationService.isAuthenticated());
//    }
//
//    /**
//     * Проверяет получение аутентифицированного пользователя.
//     */
//
//    @Test
//    void getCurrentUser_whenAuthenticated_shouldReturnUser() {
//        User user = new User("testuser", "password", false);
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
//
//        authenticationService.authenticate("testuser", "password");
//        User currentUser = authenticationService.getCurrentUser();
//        assertEquals(user, currentUser);
//    }
//
//    /**
//     * Проверяет получение не аутентифицированного пользователя.
//     */
//
//    @Test
//    void getCurrentUser_whenNotAuthenticated_shouldThrowException() {
//        assertThrows(IllegalStateException.class, () -> authenticationService.getCurrentUser());
//    }
//
//    /**
//     * Проверяет разлогинивание пользователя.
//     */
//
//    @Test
//    void logout_shouldClearCurrentUser() {
//        User user = new User("testuser", "password", false);
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
//
//        authenticationService.authenticate("testuser", "password");
//        assertTrue(authenticationService.isAuthenticated());
//
//        authenticationService.logout();
//        assertFalse(authenticationService.isAuthenticated());
//    }
//}