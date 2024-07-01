package ru.ylab.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ru.ylab.model.ConferenceRoom;
import ru.ylab.model.Resource;
import ru.ylab.model.WorkSpace;
import ru.ylab.util.DatabaseManager;

/**
 * Repository class for managing Resources entities in the database.
 * This class provides CRUD operations for Resources objects.
 */
public class ResourceRepository {

    private final DatabaseManager databaseManager;

    public ResourceRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    /**
     * Adds a new resource to the database.
     *
     * @param resource the Resource object to add
     * @throws SQLException if a database access error occurs
     */
    public void addResource(Resource resource) throws SQLException {
        String sql = "INSERT INTO coworking_schema.resources(" +
                "name, " +
                "capacity, " +
                "type) " +
                "VALUES (?,?,?) " +
                "RETURNING id";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, resource.getName());
            statement.setInt(2, resource.getCapacity());
            statement.setString(3, resource.getClass().getSimpleName());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    resource.setId(resultSet.getInt("id"));
                }
            }
        }
    }

    /**
     * Retrieves all resources from the database with pagination.
     *
     * @param offset the number of records to skip
     * @param limit  the maximum number of records to return
     * @return a List of Resource objects from the database
     * @throws SQLException             if a database access error occurs
     * @throws IllegalArgumentException if offset is negative or limit is less than 1
     */
    public List<Resource> getAllResources(int offset, int limit) throws SQLException {
        if (offset < 0 || limit < 1) {
            throw new IllegalArgumentException("Offset must be non-negative and limit must be positive");
        }
        String sql = "SELECT * FROM coworking_schema.resources OFFSET ? LIMIT ?";
        List<Resource> resources = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, offset);
            statement.setInt(2, limit);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resources.add(createResourceFromResultSet(resultSet));
                }
            }
        }
        return resources;
    }

    /**
     * Retrieves all resources from the database with default pagination.
     * This method uses a default offset of 0 and a default limit of 1000.
     *
     * @return a List of Resource objects from the database (maximum 1000 records)
     * @throws SQLException if a database access error occurs
     */
    public List<Resource> getAllResources() throws SQLException {
        return getAllResources(0, 1000);
    }

    /**
     * Retrieves resources by type from the database.
     *
     * @param type the type of resources to retrieve
     * @return a List of Resource objects in the database
     * @throws SQLException if a database access error occurs
     */
    public List<Resource> getResourcesByType(String type) throws SQLException {
        String sql = "SELECT * FROM coworking_schema.resources WHERE type = ?";
        List<Resource> resources = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, type);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    resources.add(createResourceFromResultSet(resultSet));
                }
            }
        }
        return resources;
    }

    /**
     * Finds a resource by their ID.
     *
     * @param id the ID to search for
     * @return an Optional containing the Resource if found, or an empty Optional if not found
     * @throws SQLException if a database access error occurs
     */
    public Optional<Resource> findById(int id) throws SQLException {
        String sql = "SELECT * FROM coworking_schema.resources WHERE id =?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(createResourceFromResultSet(resultSet));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Update a resource from the database.
     *
     * @param resource the Resource object to update
     * @throws SQLException if a database access error occurs
     */
    public void updateResource(Resource resource) throws SQLException {
        String sql = "UPDATE coworking_schema.resources SET " +
                "name = ?, " +
                "capacity = ?, " +
                "type=? " +
                "WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, resource.getName());
            statement.setInt(2, resource.getCapacity());
            statement.setString(3, resource.getClass().getSimpleName());
            statement.setInt(4, resource.getId());
            statement.executeUpdate();
        }
    }

    /**
     * Delete a resource from the database.
     *
     * @param resource the Resource object to delete
     * @throws SQLException if a database access error occurs
     */
    public void deleteResource(Resource resource) throws SQLException {
        String sql = "DELETE FROM coworking_schema.resources WHERE id = ?";
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, resource.getId());
            statement.executeUpdate();
        }
    }

    public enum ResourceType {
        WORKSPACE, CONFERENCE_ROOM
    }

    /**
     * Creates a Resource object from a ResultSet.
     *
     * @param resultSet the ResultSet containing resource data
     * @return a new Resource object
     * @throws SQLException if a database access error occurs
     */
    private Resource createResourceFromResultSet(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        int capacity = resultSet.getInt("capacity");
        ResourceType type = ResourceType.valueOf(resultSet.getString("type").toUpperCase());

        switch (type) {
            case WORKSPACE:
                return new WorkSpace(id, name, capacity);
            case CONFERENCE_ROOM:
                return new ConferenceRoom(id, name, capacity);
            default:
                throw new IllegalArgumentException("Unknown resource type: " + type);
        }
    }
}
