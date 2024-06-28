package ru.ylab.util;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * The DatabaseManager class is responsible for managing database connections and migrations.
 * It provides methods to initialize the database, load configuration properties,
 * run Liquibase migrations, and establish database connections.
 */
public abstract class DatabaseManager {
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;
    private static String dbSchema;
    private static String changeLog;

    /**
     * Initializes the database by loading properties and running Liquibase migrations.
     *
     * @throws Exception if an error occurs during initialization
     */
    public static void initializeDatabase() throws Exception {
        loadProperties();
        runLiquibaseMigrations();
    }

    /**
     * Loads database configuration properties from the config.properties file.
     *
     * @throws Exception if an error occurs while loading properties
     */
    private static void loadProperties() throws Exception {
        Properties props = new Properties();
        props.load(DatabaseManager.class.getClassLoader().getResourceAsStream("config.properties"));

        dbUrl = props.getProperty("db.url");
        dbUsername = props.getProperty("db.username");
        dbPassword = props.getProperty("db.password");
        dbSchema = props.getProperty("db.schema");
        changeLog = props.getProperty("changelog");
    }

    /**
     * Runs Liquibase migrations to update the database schema.
     *
     * @throws Exception if an error occurs during migration
     */
    private static void runLiquibaseMigrations() throws Exception {
        try (Connection connection = getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase = new Liquibase(changeLog, new ClassLoaderResourceAccessor(), database);
            liquibase.update("");
        }
    }

    /**
     * Establishes and returns a database connection.
     *
     * @return a Connection object representing the database connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }
}

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
//public abstract class DatabaseManager {
//    private static String dbUrl;
//    private static String dbUsername;
//    private static String dbPassword;
//    private static String dbSchema;
//
//    public static void initializeDatabase() throws Exception {
//        loadProperties();
//        runLiquibaseMigrations();
//    }
//
//    private static void loadProperties() throws Exception{
//        Properties props = new Properties();
//        props.load(DatabaseManager.class.getClassLoader().getResourceAsStream("config.properties"));
//
//        dbUrl = props.getProperty("db.url");
//        dbUsername= props.getProperty("db.username");
//        dbPassword= props.getProperty("db.password");
//        dbSchema= props.getProperty("db.schema");
//    }
//
//    private static void runLiquibaseMigrations() throws Exception{
//        try (Connection connection = getConnection()) {
//            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
//            Liquibase liquibase  = new Liquibase("db/changelog/changelog-master.xml", new ClassLoaderResourceAccessor(), database);
//            liquibase.update("");
//        }
//    }
//
//    public static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
//    }
//
//}
