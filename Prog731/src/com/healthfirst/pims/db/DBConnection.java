package com.healthfirst.pims.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Single point of JDBC connectivity for the whole application.
 *
 * Every DAO calls {@link #getConnection()} rather than dealing with
 * DriverManager directly - this keeps the connection details (URL,
 * username, password) in exactly one place, which is the pattern the
 * Programming 732 study guide describes in the JDBC chapter
 * (DriverManager.getConnection(url, user, password)).
 *
 * EDIT THE THREE CONSTANTS BELOW to match your own MySQL setup.
 */
public class DBConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/pims?useSSL=false&serverTimezone=UTC";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";

    // Load the driver once, when the class is first used.
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC driver not found on the classpath. " +
                    "Add mysql-connector-j-x.x.x.jar to your project's libraries.");
        }
    }

    private DBConnection() {
        // utility class - no instances
    }

    /**
     * Opens a brand-new connection to the "pims" database.
     * Callers are responsible for closing it (try-with-resources is
     * used throughout the DAO classes).
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
