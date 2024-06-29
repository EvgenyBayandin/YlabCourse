package ru.ylab.repository;

import ru.ylab.model.User;
import ru.ylab.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository class for managing User entities in the database.
 * This class provides CRUD operations for User objects.
 */
public class UserRepository {

    private final DatabaseManager databaseManager;

    /**
     * Constructs a new UserRepository with the given DatabaseManager.
     *
     * @param databaseManager the DatabaseManager to use for database operations
     */
    public UserRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Adds a new user to the database.
     *
     * @param user the User object to add
     * @return the User object with its ID set
     * @throws SQLException if a database access error occurs
     */
    public User addUser(User user) throws SQLException {
        String sql = "INSERT INTO coworking_schema.users (" +
                "username, " +
                "password, " +
                "is_admin) " +
                "VALUES (?, ?, ?) " +
                "RETURNING id";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setBoolean(3, user.isAdmin());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    user.setId(resultSet.getInt("id"));
                }
            }
        }
        return user;
    }

    /**
     * Finds a user by their username.
     *
     * @param username the username to search for
     * @return an Optional containing the User if found, or an empty Optional if not found
     * @throws SQLException if a database access error occurs
     */
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM coworking_schema.users WHERE username=?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(createUserFromResultSet(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Retrieves all users from the database.
     *
     * @param offset the number of records to skip
     * @param limit  the maximum number of records to return
     * @return a List of all User objects in the database
     * @throws SQLException if a database access error occurs
     */
    public List<User> getAllUsers(int offset, int limit) throws SQLException {
        if (offset < 0 || limit < 1) {
            throw new IllegalArgumentException("Offset must be non-negative and limit must be positive");
        }
        String sql = "SELECT * FROM coworking_schema.users OFFSET ? LIMIT ?";
        List<User> users = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, offset);
            statement.setInt(2, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(createUserFromResultSet(resultSet));
                }
            }
        }
        return users;
    }

    /**
     * Retrieves all resources from the database with default pagination.
     * This method uses a default offset of 0 and a default limit of 1000.
     *
     * @return a List of User objects from the database (maximum 1000 records)
     * @throws SQLException if a database access error occurs
     */
    public List<User> getAllUsers() throws SQLException {
        return getAllUsers(0, 1000);
    }

    /**
     * Updates an existing user in the database.
     *
     * @param user the User object with updated information
     * @throws SQLException if a database access error occurs
     */
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE coworking_schema.users SET " +
                "username=?, " +
                "password=?, " +
                "is_admin=? " +
                "WHERE id=?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPassword());
            statement.setBoolean(3, user.isAdmin());
            statement.setInt(4, user.getId());
            statement.executeUpdate();
        }
    }

    /**
     * Deletes a user from the database.
     *
     * @param user the User object to delete
     * @throws SQLException if a database access error occurs
     */
    public void deleteUser(User user) throws SQLException {
        String sql = "DELETE FROM coworking_schema.users WHERE id=?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.getId());
            statement.executeUpdate();
        }
    }

    /**
     * Finds a user by their ID.
     *
     * @param id the ID to search for
     * @return an Optional containing the User if found, or an empty Optional if not found
     * @throws SQLException if a database access error occurs
     */
    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM coworking_schema.users WHERE id=?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(createUserFromResultSet(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Creates a User object from a ResultSet.
     *
     * @param resultSet the ResultSet containing user data
     * @return a new User object
     * @throws SQLException if a database access error occurs
     */
    private User createUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPassword(resultSet.getString("password"));
        user.setAdmin(resultSet.getBoolean("is_admin"));
        return user;
    }
}
