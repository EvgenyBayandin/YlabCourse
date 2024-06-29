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
import ru.ylab.model.ConferenceRoom;
import ru.ylab.model.Resource;
import ru.ylab.model.WorkSpace;
import ru.ylab.util.DatabaseManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ResourceRepositoryTest.
 * These tests use Testcontainers to spin up a PostgreSQL database for each test.
 * Ensure Docker is running on your machine before executing these tests.
 */
@Testcontainers
public class ResourceRepositoryTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private ResourceRepository repository;
    private TestDatabaseManager databaseManager;

    @BeforeEach
    void setUp() throws SQLException {
        Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword());
        databaseManager = new TestDatabaseManager(connection);
        repository = new ResourceRepository(databaseManager);

        // Create tables
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS coworking_schema");
            statement.execute("CREATE TABLE IF NOT EXISTS coworking_schema.resources (" +
                    "id SERIAL PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "capacity INT NOT NULL, " +
                    "type VARCHAR(50) NOT NULL)");
        }
    }

    @Test
    void testAddAndRetrieveResource() throws SQLException {
        WorkSpace workspace = new WorkSpace(0, "Test Workspace", 10);
        repository.addResource(workspace);

        Optional<Resource> retrievedResource = repository.findById(workspace.getId());
        assertTrue(retrievedResource.isPresent());
        assertEquals(workspace.getName(), retrievedResource.get().getName());
        assertEquals(workspace.getCapacity(), retrievedResource.get().getCapacity());
        assertTrue(retrievedResource.get() instanceof WorkSpace);
    }

    @Test
    void testGetAllResources() throws SQLException {
        WorkSpace workspace = new WorkSpace(0, "Test Workspace", 10);
        ConferenceRoom conferenceRoom = new ConferenceRoom(0, "Test Conference Room", 20);
        repository.addResource(workspace);
        repository.addResource(conferenceRoom);

        List<Resource> resources = repository.getAllResources();
        assertEquals(2, resources.size());
    }

    @Test
    void testGetResourcesByType() throws SQLException {
        WorkSpace workspace1 = new WorkSpace(0, "Workspace 1", 10);
        WorkSpace workspace2 = new WorkSpace(0, "Workspace 2", 15);
        ConferenceRoom conferenceRoom = new ConferenceRoom(0, "Conference Room", 20);
        repository.addResource(workspace1);
        repository.addResource(workspace2);
        repository.addResource(conferenceRoom);

        List<Resource> workspaces = repository.getResourcesByType("WorkSpace");
        assertEquals(2, workspaces.size());
        assertTrue(workspaces.stream().allMatch(r -> r instanceof WorkSpace));

        List<Resource> conferenceRooms = repository.getResourcesByType("ConferenceRoom");
        assertEquals(1, conferenceRooms.size());
        assertTrue(conferenceRooms.stream().allMatch(r -> r instanceof ConferenceRoom));
    }

    @Test
    void testUpdateResource() throws SQLException {
        WorkSpace workspace = new WorkSpace(0, "Test Workspace", 10);
        repository.addResource(workspace);

        workspace.setName("Updated Workspace");
        workspace.setCapacity(15);
        repository.updateResource(workspace);

        Optional<Resource> updatedResource = repository.findById(workspace.getId());
        assertTrue(updatedResource.isPresent());
        assertEquals("Updated Workspace", updatedResource.get().getName());
        assertEquals(15, updatedResource.get().getCapacity());
    }

    @Test
    void testDeleteResource() throws SQLException {
        WorkSpace workspace = new WorkSpace(0, "Test Workspace", 10);
        repository.addResource(workspace);

        repository.deleteResource(workspace);

        Optional<Resource> deletedResource = repository.findById(workspace.getId());
        assertFalse(deletedResource.isPresent());
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