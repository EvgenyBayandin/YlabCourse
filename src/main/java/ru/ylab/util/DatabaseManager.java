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
//            System.out.println("Attempting to connect to: " + dbUrl);
//            System.out.println("Username: " + dbUsername);

            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
//            System.out.println("Connection established successfully");
        } catch (SQLException e) {
//            System.out.println("Failed to connect to: " + dbUrl);
//            System.out.println("Username: " + dbUsername);
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

//package ru.ylab.util;
//
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.api.command.CreateContainerResponse;
//import com.github.dockerjava.api.model.ExposedPort;
//import com.github.dockerjava.api.model.HostConfig;
//import com.github.dockerjava.api.model.PortBinding;
//import com.github.dockerjava.core.DefaultDockerClientConfig;
//import com.github.dockerjava.core.DockerClientBuilder;
//import com.github.dockerjava.core.DockerClientConfig;
//import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
//import java.time.Duration;
//import liquibase.Liquibase;
//import liquibase.database.Database;
//import liquibase.database.DatabaseFactory;
//import liquibase.database.jvm.JdbcConnection;
//import liquibase.resource.ClassLoaderResourceAccessor;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.util.Properties;
//
//public class DatabaseManager {
//
//    private static String dbUrl;
//    private static String dbName;
//    private static String dbUsername;
//    private static String dbPassword;
//    private static String dbSchema;
//    private static String changeLog;
//    private static DockerClient dockerClient;
//    private static String containerId;
//
//    public static void initializeDatabase() throws Exception {
//        loadProperties();
////        startDockerContainer();
//        runLiquibaseMigrations();
//    }
//
//    private static void loadProperties() throws Exception {
//        Properties props = new Properties();
//        props.load(DatabaseManager.class
//                .getClassLoader()
//                .getResourceAsStream("config.properties"));
//
//        dbUrl = props.getProperty("db.url");
//        dbName = props.getProperty("db.name");
//        dbUsername = props.getProperty("db.username");
//        dbPassword = props.getProperty("db.password");
//        dbSchema = props.getProperty("db.schema");
//        changeLog = props.getProperty("changelog");
//
//    }
//
//
////    private static void startDockerContainer() {
////        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
////
////        dockerClient = DockerClientBuilder
////                .getInstance(config)
////                .withDockerHttpClient(
////                        new ApacheDockerHttpClient.Builder()
////                                .dockerHost(config.getDockerHost())
////                                .sslConfig(config.getSSLConfig())
////                                .maxConnections(100)
////                                .connectionTimeout(Duration.ofSeconds(30))
////                                .responseTimeout(Duration.ofSeconds(45))
////                                .build()
////                )
////                .build();
////
////        String imageName = "postgres:13";
////
////        // Проверяем наличие образа
////        boolean imageExists = dockerClient.listImagesCmd().withImageNameFilter(imageName).exec().size() > 0;
////
////        if (!imageExists) {
////            System.out.println("Pulling PostgreSQL image...");
////            try {
////                dockerClient.pullImageCmd(imageName).start().awaitCompletion();
////                System.out.println("PostgreSQL image pulled successfully.");
////            } catch (InterruptedException e) {
////                Thread.currentThread().interrupt();
////                throw new RuntimeException("Failed to pull PostgreSQL image", e);
////            }
////        }
////
////        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
////                .withEnv("POSTGRES_DB=" + dbSchema,
////                        "POSTGRES_USER=" + dbUsername,
////                        "POSTGRES_PASSWORD=" + dbPassword)
////                .withExposedPorts(ExposedPort.tcp(5432))
////                .withHostConfig(HostConfig.newHostConfig()
////                        .withPortBindings(PortBinding.parse("5432:5432")))
////                .exec();
////
////        containerId = container.getId();
////        dockerClient.startContainerCmd(containerId).exec();
////
////        System.out.println("PostgreSQL container started. ID: " + containerId);
////    }
//
//    private static void runLiquibaseMigrations() throws Exception {
//        try (Connection connection = getConnection()) {
//            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
//            Liquibase liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database);
//            liquibase.update("");
//        }
//    }
//
////    public static Connection getConnection() throws SQLException {
////        Connection connection = null;
////        try {
////            System.out.println("Attempting to connect to: " + dbUrl);
////            System.out.println("Username: " + dbUsername);
////            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
////            System.out.println("Connection established successfully");
////
////        } catch (SQLException e) {
////            e.printStackTrace();
////            System.out.println("Failed to connect to: " + dbUrl);
////            System.out.println("Username: " + dbUsername);
////        }
//////        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
////        return connection;
////    }
//
//    public static Connection getConnection() throws SQLException {
//        Connection connection = null;
//        try {
////            String dbUrl = DatabaseConfig.getUrl();
////            String dbUsername = DatabaseConfig.getUsername();
////            String dbPassword = DatabaseConfig.getPassword();
//
//            System.out.println("Attempting to connect to: " + dbUrl);
//            System.out.println("Username: " + dbUsername);
//
//            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
//            System.out.println("Connection established successfully");
//        } catch (SQLException e) {
//            System.out.println("Failed to connect to: " + dbUrl);
//            System.out.println("Username: " +dbUsername);
//            throw e;
//        }
//        return connection;
//    }
//
//    public static void stopAndRemoveContainer() {
//        if (containerId != null && dockerClient != null) {
//            dockerClient.stopContainerCmd(containerId).exec();
//            dockerClient.removeContainerCmd(containerId).exec();
//            System.out.println("PostgreSQL container stopped and removed.");
//        }
//    }
//}

//package ru.ylab.util;
//
//import liquibase.Liquibase;
//import liquibase.database.Database;
//import liquibase.database.DatabaseFactory;
//import liquibase.database.jvm.JdbcConnection;
//import liquibase.resource.ClassLoaderResourceAccessor;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.util.Properties;
//
///**
// * The DatabaseManager class is responsible for managing database connections and migrations.
// * It provides methods to initialize the database, load configuration properties,
// * run Liquibase migrations, and establish database connections.
// */
//public abstract class DatabaseManager {
//    private static String dbUrl;
//    private static String dbUsername;
//    private static String dbPassword;
//    private static String dbSchema;
//    private static String changeLog;
//
//    /**
//     * Initializes the database by loading properties and running Liquibase migrations.
//     *
//     * @throws Exception if an error occurs during initialization
//     */
//    public static void initializeDatabase() throws Exception {
//        loadProperties();
//        runLiquibaseMigrations();
//    }
//
//    /**
//     * Loads database configuration properties from the config.properties file.
//     *
//     * @throws Exception if an error occurs while loading properties
//     */
//    private static void loadProperties() throws Exception {
//        Properties props = new Properties();
//        props.load(DatabaseManager.class
//                .getClassLoader()
//                .getResourceAsStream("config.properties"));
//
//        dbUrl = props.getProperty("db.url");
//        dbUsername = props.getProperty("db.username");
//        dbPassword = props.getProperty("db.password");
//        dbSchema = props.getProperty("db.schema");
//        changeLog = props.getProperty("changelog");
//    }
//
//    /**
//     * Runs Liquibase migrations to update the database schema.
//     *
//     * @throws Exception if an error occurs during migration
//     */
//    private static void runLiquibaseMigrations() throws Exception {
//        try (Connection connection = getConnection()) {
//            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
//            Liquibase liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database);
//            liquibase.update("");
//        }
//    }
//
//    /**
//     * Establishes and returns a database connection.
//     *
//     * @return a Connection object representing the database connection
//     * @throws SQLException if a database access error occurs
//     */
//    public static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
//    }
//
//}
