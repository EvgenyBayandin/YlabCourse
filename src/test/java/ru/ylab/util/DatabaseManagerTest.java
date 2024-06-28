package ru.ylab.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DatabaseManager.
 * These tests use Testcontainers to spin up a PostgreSQL database for each test.
 * Ensure Docker is running on your machine before executing these tests.
 */
@Testcontainers
class DatabaseManagerTest {

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @BeforeAll
    static void setupDatabase() throws Exception {
        System.setProperty("db.url", postgres.getJdbcUrl());
        System.setProperty("db.username", postgres.getUsername());
        System.setProperty("db.password", postgres.getPassword());
        System.setProperty("db.schema", "public");

        DatabaseManager.initializeDatabase();
    }

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection connection = DatabaseManager.getConnection();
             Statement stmt = connection.createStatement()) {
            stmt.execute("DROP SCHEMA public CASCADE; CREATE SCHEMA public;");
        }
    }

    @Test
    void testGetConnection() throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            assertNotNull(connection);
            assertTrue(connection.isValid(1));
        }
    }

    @Test
    void testInitializeDatabase() throws Exception {
        // Проверка, что база данных инициализирована корректно
        try (Connection connection = DatabaseManager.getConnection();
             Statement statement = connection.createStatement()) {
            // Проверка наличия таблицы, созданной Liquibase
            assertTrue(statement.executeQuery("SELECT to_regclass('public.users')").next());
        }
    }

}