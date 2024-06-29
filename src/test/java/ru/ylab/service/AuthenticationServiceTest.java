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
