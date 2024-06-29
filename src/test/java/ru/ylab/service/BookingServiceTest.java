package ru.ylab.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ylab.model.Booking;
import ru.ylab.model.Resource;
import ru.ylab.model.User;
import ru.ylab.repository.BookingRepository;
import ru.ylab.repository.ResourceRepository;
import ru.ylab.repository.UserRepository;
import ru.ylab.util.DatabaseManager;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
class BookingServiceTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");


    private ResourceRepository resourceRepository;
    private BookingRepository bookingRepository;
    private UserRepository userRepository;
    private BookingService bookingService;
    private TestDatabaseManager databaseManager;

    @BeforeEach
    void setUp() throws SQLException {
        Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        databaseManager = new TestDatabaseManager(connection);
        resourceRepository = mock(ResourceRepository.class);
        bookingRepository = mock(BookingRepository.class);
        userRepository = mock(UserRepository.class);

        // Create tables
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS coworking_schema");
            statement.execute("CREATE TABLE IF NOT EXISTS coworking_schema.resources (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "capacity INT NOT NULL, " +
                    "type VARCHAR(50) NOT NULL)");
        }
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS coworking_schema.bookings (" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "resource_id INT NOT NULL, " +
                    "start_time TIMESTAMP NOT NULL, " +
                    "end_time TIMESTAMP NOT NULL, " +
                    "FOREIGN KEY (user_id) REFERENCES coworking_schema.users(id), " +
                    "FOREIGN KEY (resource_id) REFERENCES coworking_schema.resources(id))");
        }
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS coworking_schema.users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "username VARCHAR(255) NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "is_admin BOOLEAN NOT NULL)");
        }
        try (var statement = connection.createStatement()) {
            statement.execute("INSERT INTO coworking_schema.users (" +
                    "username, " +
                    "password, " +
                    "is_admin) " +
                    "VALUES ('admin', 'admin', true)");
        }
        try (var statement = connection.createStatement()) {
            statement.execute("INSERT INTO coworking_schema.users (" +
                    "username, " +
                    "password, " +
                    "is_admin) " +
                    "VALUES ('user', 'user', false)");
        }
        try (var statement = connection.createStatement()) {
            statement.execute("INSERT INTO coworking_schema.resources (name, " +
                    "capacity, " +
                    "type) " +
                    "VALUES ('Meeting Room A', 100, 'Meeting Room')");
        }
        try (var statement = connection.createStatement()) {
            statement.execute("INSERT INTO coworking_schema.resources (name, " +
                    "capacity, " +
                    "type) " +
                    "VALUES ('Work Room', 100, 'Work Room')");
        }
        try (var statement = connection.createStatement()) {
            statement.execute("INSERT INTO coworking_schema.bookings (user_id, " +
                    "resource_id, " +
                    "start_time, " +
                    "end_time) " +
                    "VALUES (1, 1, '2023-01-01 00:00:00', '2023-01-01 01:00:00')");
        }
        try (var statement = connection.createStatement()) {
            statement.execute("INSERT INTO coworking_schema.bookings (user_id, " +
                    "resource_id, " +
                    "start_time, " +
                    "end_time) " +
                    "VALUES (2, 2, '2023-01-01 00:00:00', '2023-01-01 01:00:00')");
        }
    }

    /**
     * Проверяет создание нового бронирования без конфликтов.
     */
    @Test
    void createBooking_withoutConflict_shouldCreateBooking() throws SQLException {
        User user = new User("a", "a", true);
        Resource resource = new Resource() {
            @Override
            public int getId() {
                return 1;
            }

            @Override
            public String getName() {
                return "Meeting Room A";
            }

            @Override
            public int getCapacity() {
                return 100;
            }

            @Override
            public void setId(int id) {

            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        when(bookingRepository.getBookingsByResources(resource)).thenReturn(List.of());

        Booking result = bookingService.createBooking(user, resource, start, end);

        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getResource()).isEqualTo(resource);
        assertThat(result.getStartTime()).isEqualTo(start);
        assertThat(result.getEndTime()).isEqualTo(end);

        verify(bookingRepository).addBooking(result);
    }

    /**
     * Проверяет, что при попытке создать конфликтующее бронирование
     * выбрасывается исключение {@link IllegalStateException}.
     */
    @Test
    void createBooking_withConflict_shouldThrowException() throws SQLException {
        User user = new User("a", "a", true);
        Resource resource = new Resource() {
            @Override
            public int getId() {
                return 1;
            }

            @Override
            public String getName() {
                return "Meeting Room A";
            }

            @Override
            public int getCapacity() {
                return 100;
            }

            @Override
            public void setId(int id) {

            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        LocalDateTime end = start.plusHours(2);

        Booking existingBooking = new Booking(1, user, resource, start.minusMinutes(30), end.minusMinutes(30));
        when(bookingRepository.getBookingsByResources(resource)).thenReturn(List.of(existingBooking));

        assertThatThrownBy(() -> bookingService.createBooking(user, resource, start, end))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Booking conflict");
    }

    /**
     * Проверяет отмену бронирования.
     */
    @Test
    void cancelBooking_shouldDeleteBooking() throws SQLException {
        Booking booking = new Booking(1, new User("a", "a", true), new Resource() {
            @Override
            public int getId() {
                return 1;
            }

            @Override
            public String getName() {
                return "Room A";
            }

            @Override
            public int getCapacity() {
                return 200;
            }

            @Override
            public void setId(int id) {

            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        },
                LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        bookingService.cancelBooking(booking);

        verify(bookingRepository).deleteBooking(booking);
    }

    /**
     * Проверяет получение списка бронирований для конкретного пользователя.
     */
    @Test
    void getBookingsByUser_shouldReturnUserBookings() throws SQLException {
        User user = new User("a", "a", true);
        List<Booking> expectedBookings = Arrays.asList(
                new Booking(1, user, new Resource() {
                    @Override
                    public int getId() {
                        return 1;
                    }

                    @Override
                    public String getName() {
                        return "Room A";
                    }

                    @Override
                    public int getCapacity() {
                        return 200;
                    }

                    @Override
                    public void setId(int id) {

                    }

                    @Override
                    public void setName(String name) {

                    }

                    @Override
                    public void setCapacity(int capacity) {

                    }
                }, LocalDateTime.now(), LocalDateTime.now().plusHours(1)),
                new Booking(2, user, new Resource() {
                    @Override
                    public int getId() {
                        return 2;
                    }

                    @Override
                    public String getName() {
                        return "Room B";
                    }

                    @Override
                    public int getCapacity() {
                        return 300;
                    }

                    @Override
                    public void setId(int id) {

                    }

                    @Override
                    public void setName(String name) {

                    }

                    @Override
                    public void setCapacity(int capacity) {

                    }
                }, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2))
        );

        when(bookingRepository.getBookingsByUser(user)).thenReturn(expectedBookings);

        List<Booking> result = bookingService.getBookingsByUser(user);

        assertThat(result).isEqualTo(expectedBookings);
    }

    /**
     * Проверяет получение списка бронирований для конкретного ресурса.
     */
    @Test
    void getBookingsByResources_shouldReturnResourceBookings() throws SQLException {
        Resource resource = new Resource() {
            @Override
            public int getId() {
                return 1;
            }

            @Override
            public String getName() {
                return "Meeting Room A";
            }

            @Override
            public int getCapacity() {
                return 100;
            }

            @Override
            public void setId(int id) {

            }

            @Override
            public void setName(String name) {

            }

            @Override
            public void setCapacity(int capacity) {

            }
        };
        List<Booking> expectedBookings = Arrays.asList(
                new Booking(1, new User("a", "a", true), resource, LocalDateTime.now(), LocalDateTime.now().plusHours(1)),
                new Booking(2, new User("u", "u", false), resource, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2))
        );

        when(bookingRepository.getBookingsByResources(resource)).thenReturn(expectedBookings);

        List<Booking> result = bookingService.getBookingsByResource(resource);

        assertThat(result).isEqualTo(expectedBookings);
    }

    /**
     * Проверяет получение списка бронирований на конкретную дату.
     */
    @Test
    void getBookingsByDate_shouldReturnBookingsForSpecificDate() throws SQLException {
        LocalDate targetDate = LocalDate.now();
        List<Booking> allBookings = Arrays.asList(
                new Booking(1, new User("a", "a", true), new Resource() {
                    @Override
                    public int getId() {
                        return 1;
                    }

                    @Override
                    public String getName() {
                        return "Room A";
                    }

                    @Override
                    public int getCapacity() {
                        return 200;
                    }

                    @Override
                    public void setId(int id) {

                    }

                    @Override
                    public void setName(String name) {

                    }

                    @Override
                    public void setCapacity(int capacity) {

                    }
                },
                        targetDate.atTime(10, 0), targetDate.atTime(11, 0)),
                new Booking(2, new User("u", "u", false), new Resource() {
                    @Override
                    public int getId() {
                        return 2;
                    }

                    @Override
                    public String getName() {
                        return "Room B";
                    }

                    @Override
                    public int getCapacity() {
                        return 300;
                    }

                    @Override
                    public void setId(int id) {

                    }

                    @Override
                    public void setName(String name) {

                    }

                    @Override
                    public void setCapacity(int capacity) {

                    }
                },
                        targetDate.plusDays(1).atTime(14, 0), targetDate.plusDays(1).atTime(15, 0))
        );

        when(bookingRepository.getAllBookings()).thenReturn(allBookings);

        List<Booking> result = bookingService.getBookingsByDate(targetDate);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStartTime().toLocalDate()).isEqualTo(targetDate);
    }

    private static class TestDatabaseManager extends DatabaseManager {
        private static Connection connection = null;

        public TestDatabaseManager(Connection connection) {
            TestDatabaseManager.connection = connection;
        }


        public static Connection getConnection() throws SQLException {
            return connection;
        }
    }
}
