package com.library.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages the MySQL database connection using a singleton pattern.
 * Configure your database credentials here.
 */
public class DatabaseConnection {

    // ── Database configuration ─────────────────────────────────
    private static final String URL      = "jdbc:mysql://localhost:3306/library_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "";  // Change this to your MySQL password

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("✓ Database connected successfully.");
        } catch (ClassNotFoundException e) {
            System.err.println("✗ MySQL JDBC Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Returns the singleton DatabaseConnection instance.
     * Reconnects automatically if the connection was lost.
     */
    public static synchronized DatabaseConnection getInstance() {
        try {
            if (instance == null || instance.connection == null || instance.connection.isClosed()) {
                instance = new DatabaseConnection();
            }
        } catch (SQLException e) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /** Returns the raw JDBC connection */
    public Connection getConnection() {
        return connection;
    }

    /** Closes the database connection */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error closing connection: " + e.getMessage());
        }
    }
}
