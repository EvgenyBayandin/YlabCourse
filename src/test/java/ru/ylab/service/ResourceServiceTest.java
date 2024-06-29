package ru.ylab.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.ylab.model.WorkSpace;
import ru.ylab.model.ConferenceRoom;
import ru.ylab.model.Resource;
import ru.ylab.repository.ResourceRepository;
import ru.ylab.util.DatabaseManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Testcontainers
public class ResourceServiceTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private ResourceService resourceService;
    private ResourceRepository resourceRepository;
    private BookingService bookingService;
    private TestDatabaseManager databaseManager;

    @BeforeEach
    void setUp() throws SQLException {
        Connection connection = DriverManager.getConnection(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        databaseManager = new TestDatabaseManager(connection);
        resourceRepository = new ResourceRepository(databaseManager);
        bookingService = mock(BookingService.class);
        resourceService = new ResourceService(resourceRepository, bookingService);

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
    void testAddAndGetResource() throws SQLException {
        WorkSpace workspace = new WorkSpace(0, "Test Workspace", 10);
        resourceService.addResource(workspace);

        Resource retrievedResource = resourceService.getResourceById(workspace.getId());
        assertNotNull(retrievedResource);
        assertEquals(workspace.getName(), retrievedResource.getName());
        assertEquals(workspace.getCapacity(), retrievedResource.getCapacity());
        assertTrue(retrievedResource instanceof WorkSpace);
    }

    @Test
    void testGetAllResources() throws SQLException {
        WorkSpace workspace = new WorkSpace(0, "Test Workspace", 10);
        ConferenceRoom conferenceRoom = new ConferenceRoom(0, "Test Conference Room", 20);
        resourceService.addResource(workspace);
        resourceService.addResource(conferenceRoom);

        List<Resource> resources = resourceService.getAllResources();
        assertEquals(2, resources.size());
    }

    @Test
    void testGetResourcesByType() throws SQLException {
        WorkSpace workspace1 = new WorkSpace(0, "Workspace 1", 10);
        WorkSpace workspace2 = new WorkSpace(0, "Workspace 2", 15);
        ConferenceRoom conferenceRoom = new ConferenceRoom(0, "Conference Room", 20);
        resourceService.addResource(workspace1);
        resourceService.addResource(workspace2);
        resourceService.addResource(conferenceRoom);

        List<Resource> workspaces = resourceService.getResourcesByType("WorkSpace");
        assertEquals(2, workspaces.size());
        assertTrue(workspaces.stream().allMatch(r -> r instanceof WorkSpace));

        List<Resource> conferenceRooms = resourceService.getResourcesByType("ConferenceRoom");
        assertEquals(1, conferenceRooms.size());
        assertTrue(conferenceRooms.stream().allMatch(r -> r instanceof ConferenceRoom));
    }

    @Test
    void testUpdateResource() throws SQLException {
        WorkSpace workspace = new WorkSpace(0, "Test Workspace", 10);
        resourceService.addResource(workspace);

        workspace.setName("Updated Workspace");
        workspace.setCapacity(15);
        resourceService.updateResource(workspace);

        Resource updatedResource = resourceService.getResourceById(workspace.getId());
        assertEquals("Updated Workspace", updatedResource.getName());
        assertEquals(15, updatedResource.getCapacity());
    }

    @Test
    void testDeleteResource() throws SQLException {
        WorkSpace workspace = new WorkSpace(0, "Test Workspace", 10);
        resourceService.addResource(workspace);

        resourceService.deleteResource(workspace);

        assertThrows(IllegalArgumentException.class, () -> resourceService.getResourceById(workspace.getId()));
    }

    @Test
    void testGetAvailableResources() throws SQLException {
        WorkSpace workspace1 = new WorkSpace(0, "Workspace 1", 10);
        WorkSpace workspace2 = new WorkSpace(0, "Workspace 2", 15);
        resourceService.addResource(workspace1);
        resourceService.addResource(workspace2);

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusHours(2);

        when(bookingService.getBookingsByResource(any(Resource.class))).thenReturn(List.of());

        List<Resource> availableResources = resourceService.getAvailableResources(start, end);
        assertEquals(2, availableResources.size());
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
