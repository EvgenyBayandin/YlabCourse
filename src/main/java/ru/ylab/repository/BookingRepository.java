package ru.ylab.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.ylab.model.Booking;
import ru.ylab.model.Resource;
import ru.ylab.model.User;
import ru.ylab.util.DatabaseManager;

/**
 * Repository class for managing Bookings in the database.
 * This class provides CRUD operations for Booking objects.
 */
public class BookingRepository {

    private final DatabaseManager databaseManager;
    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;

    public BookingRepository(DatabaseManager databaseManager, UserRepository userRepository, ResourceRepository resourceRepository) {
        this.databaseManager = databaseManager;
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
    }

    /**
     * Adds a new booking to the database.
     *
     * @param booking the Resource object to add
     * @throws SQLException if a database access error occurs
     */
    public void addBooking(Booking booking) throws SQLException {
        String sql = "INSERT INTO coworking_schema.bookings (" +
                "user_id, " +
                "resource_id, " +
                "start_time, " +
                "end_time) " +
                "VALUES (?,?,?,?) " +
                "RETURNING id";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, booking.getUserId());
            statement.setInt(2, booking.getResourceId());
            statement.setTimestamp(3, Timestamp.valueOf(booking.getStartTime()));
            statement.setTimestamp(4, Timestamp.valueOf(booking.getEndTime()));
//            statement.setObject(3, booking.getStartTime());
//            statement.setObject(4, booking.getEndTime());
            statement.executeUpdate();
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    booking.setId(resultSet.getInt("id"));
                }
            }
        }
    }

    /**
     * Retrieves all bookings from the database with pagination.
     *
     * @param offset the number of records to skip
     * @param limit the maximum number of records to return
     * @return a List of Booking objects from the database
     * @throws SQLException if a database access error occurs
     * @throws IllegalArgumentException if offset is negative or limit is less than 1
     */
    public List<Booking> getAllBookings(int offset, int limit) throws SQLException {
        if (offset < 0 || limit < 1) {
            throw new IllegalArgumentException("Offset must be non-negative and limit must be positive");
        }
        String sql = "SELECT * FROM coworking_schema.bookings ORDER BY start_time DESC LIMIT ? OFFSET ?";
        List<Booking> bookings = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            statement.setInt(2, offset);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(createBookingFromResultSet(resultSet));
                }
            }
        }
        return bookings;
    }

    /**
     * Retrieves all bookings from the database with default pagination.
     * This method uses a default offset of 0 and a default limit of 1000.
     *
     * @return a List of Booking objects from the database (maximum 1000 records)
     * @throws SQLException if a database access error occurs
     */
    public List<Booking> getAllBookings() throws SQLException {
        return getAllBookings(0, 1000);
    }

    /**
     * Finds a booking by ID.
     *
     * @param id the ID to search for
     * @return an Optional containing the Booking if found, or an empty Optional if not found
     * @throws SQLException if a database access error occurs
     */
    public Optional<Booking> findById(int id) throws SQLException {
        String sql = "SELECT * FROM coworking_schema.bookings WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(createBookingFromResultSet(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Finds a bookings by user.
     *
     * @param user the User to search for
     * @return List containing the Booking if found
     * @throws SQLException if a database access error occurs
     */
    public List<Booking> getBookingsByUser(User user) throws SQLException {
        String sql = "SELECT * FROM coworking_schema.bookings WHERE user_id = ?";
        List<Booking> bookings = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(createBookingFromResultSet(resultSet));
                }
            }
        }
        return bookings;
    }

    /**
     * Finds a bookings by resource.
     *
     * @param resource the Resource to search for
     * @return List containing the Booking if found
     * @throws SQLException if a database access error occurs
     */
    public List<Booking> getBookingsByResources(Resource resource) throws SQLException {
        String sql = "SELECT * FROM coworking_schema.bookings WHERE resource_id = ?";
        List<Booking> bookings = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, resource.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(createBookingFromResultSet(resultSet));
                }
            }
        }
        return bookings;
    }

    /**
     * Finds a bookings by date.
     *
     * @param date the LocalDate to search for
     * @return List containing the Booking if found
     * @throws SQLException if a database access error occurs
     */
    public List<Booking> getBookingsByDate(LocalDate date) throws SQLException {
        String sql = "SELECT * FROM coworking_schema.bookings WHERE DATE(start_time) = ?";
        List<Booking> bookings = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, date);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    bookings.add(createBookingFromResultSet(resultSet));
                }
            }
        }
        return bookings;
    }

    /**
     * Update a booking from the database.
     *
     * @param booking the Booking object to update
     * @throws SQLException if a database access error occurs
     */
    public void updateBooking(Booking booking) throws SQLException {
        String sql = "UPDATE coworking_schema.bookings SET " +
                "user_id=?, " +
                "resource_id=?, " +
                "start_time=?, " +
                "end_time=? " +
                "WHERE id=?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, booking.getUserId());
            statement.setInt(2, booking.getResourceId());
            statement.setObject(3, booking.getStartTime());
            statement.setObject(4, booking.getEndTime());
            statement.setInt(5, booking.getId());
            statement.executeUpdate();
        }
    }

    /**
     * Delete a booking from the database.
     *
     * @param booking the Booking object to delete
     * @throws SQLException if a database access error occurs
     */
    public void deleteBooking(Booking booking) throws SQLException {
        String sql = "DELETE FROM coworking_schema.bookings WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, booking.getId());
            statement.executeUpdate();
        }
    }

    /**
     * Creates a Booking object from a ResultSet.
     *
     * @param resultSet the ResultSet containing resource data
     * @return a new Booking object
     * @throws SQLException if a database access error occurs
     */
    private Booking createBookingFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        int userId = resultSet.getInt("user_id");
        int resourceId = resultSet.getInt("resource_id");
        LocalDateTime startTime = LocalDateTime.parse(resultSet.getString("start_time"));
        LocalDateTime endTime = LocalDateTime.parse(resultSet.getString("end_time"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new SQLException("User not found for id: " + userId));
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new SQLException("Resource not found for id: " + resourceId));

        return new Booking(id, user, resource, startTime, endTime);
    }
}
