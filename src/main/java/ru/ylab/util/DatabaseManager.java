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

public class DatabaseManager {
    private static String dbUrl;
    private static String dbUsername;
    private static String dbPassword;
    private static String dbSchema;

    public static void initializeDatabase() throws Exception {
        loadProperties();
        runLiquibaseMigrations();
    }

    private static void loadProperties() throws Exception{
        Properties props = new Properties();
        props.load(DatabaseManager.class.getClassLoader().getResourceAsStream("config.properties"));

        dbUrl = props.getProperty("db.url");
        dbUsername= props.getProperty("db.username");
        dbPassword= props.getProperty("db.password");
        dbSchema= props.getProperty("db.schema");
    }

    private static void runLiquibaseMigrations() throws Exception{
        try (Connection connection = getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            Liquibase liquibase  = new Liquibase("db/changelog/changelog-master.xml", new ClassLoaderResourceAccessor(), database);
            liquibase.update("");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

}
