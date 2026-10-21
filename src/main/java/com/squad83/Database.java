package com.squad83;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides access to the PostgreSQL database used by the Squad 83 POS system.
 * The connection settings are stored as constants, and a new connection is
 * created each time one is requested.
 *
 * @author Squad 83
 */
public class Database {

    private static final String URL = "jdbc:postgresql://csce-315-db.engr.tamu.edu:5432/squad_83_db";
    private static final String USER = "squad_83";
    private static final String PASSWORD = "plazmapug";

    /**
     * Opens a new connection to the database. The PostgreSQL JDBC driver is
     * loaded first. The caller is responsible for closing the connection.
     *
     * @return a new connection to the database
     * @throws SQLException if the PostgreSQL JDBC driver cannot be found or the
     *                      connection to the database cannot be established
     */
    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found.", e);
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}