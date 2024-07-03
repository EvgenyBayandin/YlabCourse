package ru.ylab.util;

import com.github.dockerjava.api.DockerClient;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

public class DatabaseManager {

    private static String dbUrl;
    private static String dbName;
    private static String dbUsername;
    private static String dbPassword;
    private static String dbSchema;
    private static String changeLog;
    private static DockerClient dockerClient;
    private static String containerId;

    public static void initializeDatabase() throws Exception {
        loadProperties();
        runLiquibaseMigrations();
    }

    private static void loadProperties() throws Exception {
        Properties props = new Properties();
        props.load(DatabaseManager.class
                .getClassLoader()
                .getResourceAsStream("config.properties"));

        dbUrl = props.getProperty("db.url");
        dbName = props.getProperty("db.name");
        dbUsername = props.getProperty("db.username");
        dbPassword = props.getProperty("db.password");
        dbSchema = props.getProperty("db.schema");
        changeLog = props.getProperty("changelog");

    }

    private static void runLiquibaseMigrations() throws Exception {
        try (Connection connection = getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database);
            liquibase.update("");
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (SQLException e) {
            System.out.println("Failed to connect to: " + dbUrl);
            System.out.println("Username: " + dbUsername);
            throw e;
        }
        return connection;
    }

    public static void stopAndRemoveContainer() {
        if (containerId != null && dockerClient != null) {
            dockerClient.stopContainerCmd(containerId).exec();
            dockerClient.removeContainerCmd(containerId).exec();
            System.out.println("PostgreSQL container stopped and removed.");
        }
    }
}