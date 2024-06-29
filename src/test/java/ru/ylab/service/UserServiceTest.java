package ru.ylab.service;

import java.sql.DriverManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ylab.model.User;
import ru.ylab.repository.UserRepository;
import ru.ylab.util.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UserService.
 * These tests use Testcontainers to spin up a PostgreSQL database for each test.
 * Ensure Docker is running on your machine before executing these tests.
 */
@Testcontainers
class UserServiceTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private UserService userService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws SQLException {
        Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        DatabaseManager testDatabaseManager = new UserServiceTest.TestDatabaseManager(connection);
        userRepository = new UserRepository(testDatabaseManager);
        userService = new UserService(userRepository);

        // Очищаем таблицу users
        try (var statement = connection.createStatement()) {
            statement.execute("TRUNCATE TABLE users");
        }
    }

    @Test
    void testRegister() throws SQLException {
        User user = userService.register("testuser", "password");
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertFalse(user.isAdmin());
    }

    @Test
    void testRegisterDuplicateUsername() {
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register("testuser", "password");
            userService.register("testuser", "anotherpassword");
        });
    }

    @Test
    void testLogin() throws SQLException {
        userService.register("testuser", "password");
        User loggedInUser = userService.login("testuser", "password");
        assertNotNull(loggedInUser);
        assertEquals("testuser", loggedInUser.getUsername());
    }

    @Test
    void testLoginInvalidCredentials() {
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register("testuser", "password");
            userService.login("testuser", "wrongpassword");
        });
    }

    @Test
    void testChangePassword() throws SQLException {
        userService.register("testuser", "oldpassword");
        userService.changePassword("testuser", "oldpassword", "newpassword");
        User user = userService.login("testuser", "newpassword");
        assertNotNull(user);
    }

    @Test
    void testDeleteUser() throws SQLException {
        userService.register("testuser", "password");
        userService.deleteUser("testuser");
        assertThrows(IllegalArgumentException.class, () -> userService.getUser("testuser"));
    }

    @Test
    void testGetUser() throws SQLException {
        userService.register("testuser", "password");
        User user = userService.getUser("testuser");
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
    }

    class TestDatabaseManager extends DatabaseManager {
        private static Connection connection = null;

        public TestDatabaseManager(Connection connection) {
            this.connection = connection;
        }


        public static Connection getConnection() throws SQLException {
            return connection;
        }
    }

}
