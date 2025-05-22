package com.example.demo.lms.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/lms_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "123456"; // Change this to your actual password

    private static Connection connection;

    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                try {
                    Class.forName("org.postgresql.Driver");
                    connection = DriverManager.getConnection(URL, USER, PASSWORD);
                    System.out.println("Database connection established successfully");
                } catch (ClassNotFoundException e) {
                    System.err.println("PostgreSQL JDBC driver not found");
                    e.printStackTrace();
                } catch (SQLException e) {
                    System.err.println("Connection to database failed");
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking connection status");
            e.printStackTrace();
        }

        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed successfully");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection");
            e.printStackTrace();
        }
    }
}