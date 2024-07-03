package ru.ylab.ui;

import java.sql.DriverManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.SQLException;
import ru.ylab.in.InputReader;
import ru.ylab.model.User;
import ru.ylab.out.OutputWriter;
import ru.ylab.service.AuthenticationService;
import ru.ylab.service.BookingService;
import ru.ylab.service.ResourceService;
import ru.ylab.service.UserService;
import ru.ylab.util.DatabaseManager;
import ru.ylab.util.ResourceNotFoundException;

import static org.mockito.Mockito.*;

@Testcontainers
public class ConsoleUITest {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Mock
    private InputReader inputReader;

    @Mock
    private OutputWriter outputWriter;

    @Mock
    private UserService userService;

    @Mock
    private ResourceService resourceService;

    @Mock
    private BookingService bookingService;

    @Mock
    private AuthenticationService authenticationService;

    private ConsoleUI consoleUI;

    private TestDatabaseManager testDatabaseManager;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);

        Connection connection = DriverManager.getConnection(
                postgresContainer.getJdbcUrl(),
                postgresContainer.getUsername(),
                postgresContainer.getPassword()
        );

        testDatabaseManager = new TestDatabaseManager(connection);

        consoleUI = new ConsoleUI(
                inputReader,
                outputWriter,
                userService,
                resourceService,
                bookingService,
                authenticationService
        );
    }

    @Test
    void testLogin() throws SQLException, ResourceNotFoundException {

        when(inputReader.readLine()).thenReturn("testuser", "testpass");
        when(authenticationService.authenticate("testuser", "testpass"))
                .thenReturn(new User(1, "testuser", "testpass", false));

        consoleUI.run();

        verify(outputWriter).printLine("Login successful. Welcome, testuser");
    }

    @Test
    void testRegister() throws SQLException, ResourceNotFoundException {

        when(inputReader.readLine()).thenReturn("newuser", "newpass");
        when(userService.register("newuser", "newpass"))
                .thenReturn(new User(2, "newuser", "newpass", false));

        consoleUI.run();

        verify(outputWriter).printLine("Registration successful. Please login.");
    }

    /*
    ToDo не забудь добавить тесты !!!
    */

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