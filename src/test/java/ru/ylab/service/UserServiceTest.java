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

//    @BeforeEach
//    void setUp() throws SQLException {
//        Connection connection = postgres.createConnection("");
//        DatabaseManager databaseManager = new DatabaseManager() {
//            @Override
//            public Connection getConnection() throws SQLException {
//                return connection;
//            }
//        };
//        userRepository = new UserRepository(databaseManager);
//        userService = new UserService(userRepository);
//
//        // Clear the users table before each test
//        try (var statement = connection.createStatement()) {
//            statement.execute("TRUNCATE TABLE users");
//        }
//    }

    @BeforeEach
    void setUp() throws SQLException {
        Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        DatabaseManager testDatabaseManager = new UserServiceTest.TestDatabaseManager(connection);
        userRepository = new UserRepository(testDatabaseManager);
        userService  = new UserService(userRepository);

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
// * Тестовый класс для {@link UserService}.
// * Проверяет функциональность сервиса для работы с пользователями.
// */
//
//class UserServiceTest {
//
//    @Mock
//    private UserRepository userRepository;
//
//    private UserService userService;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//        userService = new UserService(userRepository);
//    }
//
//    /**
//     * Проверяет создание нового пользователя.
//     */
//
//    @Test
//    void register_newUser_success() {
//        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
//        User newUser = userService.register("newuser", "password");
//        assertNotNull(newUser);
//        assertEquals("newuser", newUser.getUsername());
//        verify(userRepository).addUser(newUser);
//    }
//
//    /**
//     * Проверяет создание нового пользователя и выбрасывает исключение если пользователь существует.
//     */
//
//    @Test
//    void register_existingUser_throwsException() {
//        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(new User("existinguser", "password", false)));
//        assertThrows(IllegalArgumentException.class, () -> userService.register("existinguser", "password"));
//    }
//
//    /**
//     * Проверяет валидность пароля пользователя.
//     */
//
//    @Test
//    void login_validCredentials_success() {
//        User user = new User("testuser", "password", false);
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
//        User loggedInUser = userService.login("testuser", "password");
//        assertEquals(user, loggedInUser);
//    }
//
//    /**
//     * Проверяет валидность имени пользователя.
//     */
//
//    @Test
//    void login_invalidUsername_throwsException() {
//        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
//        assertThrows(IllegalArgumentException.class, () -> userService.login("nonexistent", "password"));
//    }
//
//    /**
//     * Проверяет валидность при измении пароля пользователя.
//     */
//
//    @Test
//    void changePassword_validCredentials_success() {
//        User user = new User("testuser", "oldpassword", false);
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
//        userService.changePassword("testuser", "oldpassword", "newpassword");
//        assertEquals("newpassword", user.getPassword());
//        verify(userRepository).updateUser(user);
//    }
//
//    /**
//     * Проверяет валидность при измении пароля пользователя, выбрасывает исключение если пароль существует.
//     */
//
//    @Test
//    void changePassword_invalidOldPassword_throwsException() {
//        User user = new User("testuser", "correctpassword", false);
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
//        assertThrows(IllegalArgumentException.class,
//                () -> userService.changePassword("testuser", "wrongpassword", "newpassword"));
//    }
//
//    /**
//     * Проверяет валидность при удалении пользователя
//     */
//
//    @Test
//    void deleteUser_existingUser_success() {
//        User user = new User("testuser", "password", false);
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
//        userService.deleteUser("testuser");
//        verify(userRepository).deleteUser(user);
//    }
//
//    /**
//     * Проверяет валидность при удалении несуществующего пользователя
//     */
//
//    @Test
//    void deleteUser_nonexistentUser_throwsException() {
//        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
//        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser("nonexistent"));
//    }
//
//    /**
//     * Проверяет валидность при получении пользователя
//     */
//
//    @Test
//    void getUser_existingUser_returnsUser() {
//        User user = new User("testuser", "password", false);
//        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
//        User retrievedUser = userService.getUser("testuser");
//        assertEquals(user, retrievedUser);
//    }
//
//    /**
//     * Проверяет валидность при уполучении пользователя, выбрасывает исключение если пользователь существует.
//     */
//
//    @Test
//    void getUser_nonexistentUser_throwsException() {
//        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
//        assertThrows(IllegalArgumentException.class, () -> userService.getUser("nonexistent"));
//    }
//}