package ru.ylab.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ylab.model.User;
import ru.ylab.util.DatabaseManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UserRepository.
 * These tests use Testcontainers to spin up a PostgreSQL database for each test.
 * Ensure Docker is running on your machine before executing these tests.
 */
@Testcontainers
class UserRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private UserRepository userRepository;

    @BeforeEach
    void setUp() throws SQLException {
        Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        DatabaseManager testDatabaseManager = new UserRepositoryTest.TestDatabaseManager(connection);
        userRepository = new UserRepository(testDatabaseManager);

        // Создаем таблицу users для тестов
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS coworking_schema");
            statement.execute("CREATE TABLE IF NOT EXISTS coworking_schema.users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "username VARCHAR(255) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "is_admin BOOLEAN NOT NULL)");
        }
    }

    @Test
    void testAddUser() throws SQLException {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setAdmin(false);

        User addedUser = userRepository.addUser(user);

        assertNotNull(addedUser.getId());
        assertEquals("testuser", addedUser.getUsername());
    }

    @Test
    void testFindByUsername() throws SQLException {
        User user = new User();
        user.setUsername("findme");
        user.setPassword("password");
        user.setAdmin(false);
        userRepository.addUser(user);

        Optional<User> foundUser = userRepository.findByUsername("findme");

        assertTrue(foundUser.isPresent());
        assertEquals("findme", foundUser.get().getUsername());
    }

    @Test
    void testGetAllUsers() throws SQLException {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setPassword("password1");
        user1.setAdmin(false);
        userRepository.addUser(user1);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password2");
        user2.setAdmin(true);
        userRepository.addUser(user2);

        List<User> allUsers = userRepository.getAllUsers();

        assertEquals(2, allUsers.size());
    }

    @Test
    void testUpdateUser() throws SQLException {
        User user = new User();
        user.setUsername("updateme");
        user.setPassword("oldpassword");
        user.setAdmin(false);
        User addedUser = userRepository.addUser(user);

        addedUser.setPassword("newpassword");
        addedUser.setAdmin(true);
        userRepository.updateUser(addedUser);

        Optional<User> updatedUser = userRepository.findByUsername("updateme");

        assertTrue(updatedUser.isPresent());
        assertEquals("newpassword", updatedUser.get().getPassword());
        assertTrue(updatedUser.get().isAdmin());
    }

    @Test
    void testDeleteUser() throws SQLException {
        User user = new User();
        user.setUsername("deleteme");
        user.setPassword("password");
        user.setAdmin(false);
        User addedUser = userRepository.addUser(user);

        userRepository.deleteUser(addedUser);

        Optional<User> deletedUser = userRepository.findByUsername("deleteme");

        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testFindById() throws SQLException {
        User user = new User();
        user.setUsername("findbyid");
        user.setPassword("password");
        user.setAdmin(false);
        User addedUser = userRepository.addUser(user);

        Optional<User> foundUser = userRepository.findById(addedUser.getId());

        assertTrue(foundUser.isPresent());
        assertEquals("findbyid", foundUser.get().getUsername());
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