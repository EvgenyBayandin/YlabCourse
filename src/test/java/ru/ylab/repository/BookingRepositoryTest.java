package ru.ylab.repository;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import ru.ylab.model.Booking;
import ru.ylab.model.Resource;
import ru.ylab.model.WorkSpace;
import ru.ylab.model.User;
import ru.ylab.util.DatabaseManager;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class BookingRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private BookingRepository repository;
    private UserRepository userRepository;
    private ResourceRepository resourceRepository;
    private TestDatabaseManager databaseManager;


    @BeforeEach
    void setUp() throws SQLException {
        Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        databaseManager = new TestDatabaseManager(connection);
        userRepository = new UserRepository(databaseManager);
        resourceRepository = new ResourceRepository(databaseManager);
        repository = new BookingRepository(databaseManager, userRepository, resourceRepository);

        // Create tables
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS coworking_schema");
            statement.execute("CREATE TABLE IF NOT EXISTS coworking_schema.bookings(" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_id INT NOT NULL, " +
                    "resource_id INT NOT NULL, " +
                    "start_time TIMESTAMP NOT NULL, " +
                    "end_time TIMESTAMP NOT NULL)");
        }
    }

    @Test
    void testAddAndRetrieveBooking() throws SQLException {
        User user = new User(1, "testuser", "password", false);
        userRepository.addUser(user);

        Resource resource = new WorkSpace(1, "Test WorkSpace", 10);
        resourceRepository.addResource(resource);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(1);

        Booking booking = new Booking(0, user, resource, startTime, endTime);
        repository.addBooking(booking);

        assertNotEquals(0, booking.getId());
    }

    @Test
    void testGetAllBookings() throws SQLException {
        User user = new User(1, "testuser", "password", false);
        userRepository.addUser(user);

        Resource resource = new WorkSpace(1, "Test WorkSpace", 10);
        resourceRepository.addResource(resource);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(1);

        Booking booking1 = new Booking(0, user, resource, startTime, endTime);
        Booking booking2 = new Booking(0, user, resource, startTime.plusHours(2), endTime.plusHours(2));

        repository.addBooking(booking1);
        repository.addBooking(booking2);

        List<Booking> bookings = repository.getAllBookings();

        assertEquals(2, bookings.size());
    }

    @Test
    void testFindById() throws SQLException {
        User user = new User(1, "testuser", "password", false);
        userRepository.addUser(user);

        Resource resource = new WorkSpace(1, "Test WorkSpace", 10);
        resourceRepository.addResource(resource);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(1);

        Booking booking = new Booking(0, user, resource, startTime, endTime);
        repository.addBooking(booking);

        Optional<Booking> foundBooking = repository.findById(booking.getId());

        assertTrue(foundBooking.isPresent());
        assertEquals(booking.getId(), foundBooking.get().getId());
    }

    @Test
    void testGetBookingsByUser() throws SQLException {
        User user1 = new User(1, "testuser1", "password", false);
        User user2 = new User(2, "testuser2", "password", false);
        userRepository.addUser(user1);
        userRepository.addUser(user2);

        Resource resource = new WorkSpace(1, "Test WorkSpace", 10);
        resourceRepository.addResource(resource);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(1);

        Booking booking1 = new Booking(0, user1, resource, startTime, endTime);
        Booking booking2 = new Booking(0, user1, resource, startTime.plusHours(2), endTime.plusHours(2));
        Booking booking3 = new Booking(0, user2, resource, startTime.plusHours(4), endTime.plusHours(4));

        repository.addBooking(booking1);
        repository.addBooking(booking2);
        repository.addBooking(booking3);

        List<Booking> user1Bookings = repository.getBookingsByUser(user1);

        assertEquals(2, user1Bookings.size());
        assertTrue(user1Bookings.stream().allMatch(b -> b.getUser().getId() == user1.getId()));
    }

    @Test
    void testGetBookingsByResource() throws SQLException {
        User user = new User(1, "testuser", "password", false);
        userRepository.addUser(user);

        Resource resource1 = new WorkSpace(1, "WorkSpace 1", 10);
        Resource resource2 = new WorkSpace(2, "WorkSpace 2", 15);
        resourceRepository.addResource(resource1);
        resourceRepository.addResource(resource2);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(1);

        Booking booking1 = new Booking(0, user, resource1, startTime, endTime);
        Booking booking2 = new Booking(0, user, resource1, startTime.plusHours(2), endTime.plusHours(2));
        Booking booking3 = new Booking(0, user, resource2, startTime.plusHours(4), endTime.plusHours(4));

        repository.addBooking(booking1);
        repository.addBooking(booking2);
        repository.addBooking(booking3);

        List<Booking> resource1Bookings = repository.getBookingsByResources(resource1);

        assertEquals(2, resource1Bookings.size());
        assertTrue(resource1Bookings.stream().allMatch(b -> b.getResource().getId() == resource1.getId()));
    }

    @Test
    void testGetBookingsByDate() throws SQLException {
        User user = new User(1, "testuser", "password", false);
        userRepository.addUser(user);

        Resource resource = new WorkSpace(1, "WorkSpace", 10);
        resourceRepository.addResource(resource);

        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime tomorrow = today.plusDays(1);

        Booking booking1 = new Booking(0, user, resource, today.plusHours(10), today.plusHours(11));
        Booking booking2 = new Booking(0, user, resource, today.plusHours(14), today.plusHours(15));
        Booking booking3 = new Booking(0, user, resource, tomorrow.plusHours(10), tomorrow.plusHours(11));

        repository.addBooking(booking1);
        repository.addBooking(booking2);
        repository.addBooking(booking3);

        List<Booking> todayBookings = repository.getBookingsByDate(today.toLocalDate());

        assertEquals(2, todayBookings.size());
        assertTrue(todayBookings.stream().allMatch(b -> b.getStartTime().toLocalDate().equals(today.toLocalDate())));
    }

    @Test
    void testUpdateBooking() throws SQLException {
        User user = new User(1, "testuser", "password", false);
        userRepository.addUser(user);

        Resource resource = new WorkSpace(1, "WorkSpace", 10);
        resourceRepository.addResource(resource);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(1);

        Booking booking = new Booking(0, user, resource, startTime, endTime);
        repository.addBooking(booking);

        LocalDateTime newStartTime = startTime.plusHours(2);
        LocalDateTime newEndTime = endTime.plusHours(2);
        booking.setStartTime(newStartTime);
        booking.setEndTime(newEndTime);

        repository.updateBooking(booking);

        Optional<Booking> updatedBooking = repository.findById(booking.getId());

        assertTrue(updatedBooking.isPresent());
        assertEquals(newStartTime, updatedBooking.get().getStartTime());
        assertEquals(newEndTime, updatedBooking.get().getEndTime());
    }

    @Test
    void testDeleteBooking() throws SQLException {
        User user = new User(1, "testuser", "password", false);
        userRepository.addUser(user);

        Resource resource = new WorkSpace(1, "WorkSpace", 10);
        resourceRepository.addResource(resource);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDateTime endTime = startTime.plusHours(1);

        Booking booking = new Booking(0, user, resource, startTime, endTime);
        repository.addBooking(booking);

        repository.deleteBooking(booking);

        Optional<Booking> deletedBooking = repository.findById(booking.getId());

        assertFalse(deletedBooking.isPresent());
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